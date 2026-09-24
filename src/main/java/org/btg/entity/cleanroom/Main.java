package org.btg.entity.cleanroom;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public final class Main {
  static final ObjectMapper M = new ObjectMapper();
  static final String[] REQUIRED = {"transaction_record","manifests","asset_provenance","rights","licence","usage_receipt","license_settlement","value_record","digital_commodity","corporate_authorization","capital","share_settlement","event_ledger","external_trust_anchors"};
  static final Set<String> KINDS = Set.of("SCHEMA","RIGHT","EVENT","CAPABILITY","ASSET_CLASS","TRUST_FRAMEWORK","DISPUTE_AUTHORITY","ATTESTATION_CLASS");
  static final Set<String> TOPOLOGY = Set.of("CORE","REGIONAL","EDGE","SATELLITE","OFFLINE");
  static final Set<String> SCARCITY = Set.of("RIGHT","ENTITLEMENT","CAPACITY","DURATION","JURISDICTION","USAGE_QUANTITY","DERIVATION","PARTICIPATION","TRANSFERABILITY");

  static JsonNode load(Path p) throws IOException { return M.readTree(Files.readAllBytes(p)); }
  static String s(JsonNode n,String k){ JsonNode v=n==null?null:n.get(k); return v!=null&&v.isTextual()?v.asText():""; }
  static long i(JsonNode n,String k){ JsonNode v=n==null?null:n.get(k); return v!=null&&v.isNumber()?v.asLong():0L; }
  static boolean b(JsonNode n,String k){ JsonNode v=n==null?null:n.get(k); return v!=null&&v.isBoolean()&&v.asBoolean(); }
  static ArrayNode a(JsonNode n,String k){ JsonNode v=n==null?null:n.get(k); return v instanceof ArrayNode?(ArrayNode)v:M.createArrayNode(); }
  static ObjectNode o(JsonNode n,String k){ JsonNode v=n==null?null:n.get(k); return v instanceof ObjectNode?(ObjectNode)v:M.createObjectNode(); }
  static String canonical(JsonNode n) throws Exception {
    if(n==null||n.isNull()) return "null";
    if(n.isObject()){
      List<String> keys=new ArrayList<>(); n.fieldNames().forEachRemaining(keys::add); Collections.sort(keys);
      StringBuilder z=new StringBuilder("{"); boolean first=true;
      for(String k:keys){ if(!first)z.append(','); first=false; z.append(M.writeValueAsString(k)).append(':').append(canonical(n.get(k))); }
      return z.append('}').toString();
    }
    if(n.isArray()){
      StringBuilder z=new StringBuilder("["); for(int x=0;x<n.size();x++){ if(x>0)z.append(','); z.append(canonical(n.get(x))); } return z.append(']').toString();
    }
    if(n.isTextual()) return M.writeValueAsString(n.asText());
    return n.toString();
  }
  static String sha(byte[] v) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v)); }
  static String sha(String v) throws Exception { return sha(v.getBytes(StandardCharsets.UTF_8)); }
  static byte[] hex(String x){ return HexFormat.of().parseHex(x.trim()); }
  static void add(List<String> e,String c){ if(!e.contains(c))e.add(c); }
  static boolean eq(JsonNode a,String ak,JsonNode b,String bk){ return s(a,ak).equals(s(b,bk)); }

  static boolean verifySignature(ObjectNode bundle) {
    try {
      ObjectNode sg=o(bundle,"signature"); if(!"Ed25519".equals(s(sg,"alg"))) return false;
      byte[] der=Base64.getDecoder().decode(s(sg,"public_key_spki_der_b64"));
      PublicKey key=KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(der));
      ObjectNode h=M.createObjectNode(); for(String k:new String[]{"schema","transaction_id","issuer_entity_id","transaction_root_sha256"}) h.put(k,s(bundle,k));
      Signature v=Signature.getInstance("Ed25519"); v.initVerify(key); v.update(canonical(h).getBytes(StandardCharsets.UTF_8));
      return v.verify(Base64.getDecoder().decode(s(sg,"sig_b64")));
    } catch(Exception ex){ return false; }
  }
  static boolean verifyLedger(ObjectNode ledger,List<String> errors) throws Exception {
    ArrayNode events=a(ledger,"events"); String prev="0".repeat(64);
    for(int n=0;n<events.size();n++){
      ObjectNode e=(ObjectNode)events.get(n);
      if(i(e,"sequence")!=n+1){ add(errors,"LEDGER_SEQUENCE_INVALID"); return false; }
      if(!prev.equals(s(e,"prev_hash"))){ add(errors,"LEDGER_PREV_HASH_INVALID"); return false; }
      String expected=sha(prev+":"+canonical(e.get("payload")));
      if(!expected.equals(s(e,"event_hash"))){ add(errors,"LEDGER_EVENT_HASH_INVALID"); return false; }
      prev=expected;
    }
    ObjectNode cp=o(ledger,"checkpoint");
    if(i(cp,"sequence")!=events.size()||!prev.equals(s(cp,"head_hash"))){ add(errors,"LEDGER_CHECKPOINT_INVALID"); return false; }
    return true;
  }

  static ObjectNode verifyBundle(ObjectNode bundle) throws Exception {
    List<String> errors=new ArrayList<>(); ObjectNode ev=o(bundle,"evidence");
    String root=sha(canonical(ev)); boolean rootOk=root.equals(s(bundle,"transaction_root_sha256")); if(!rootOk)add(errors,"ROOT_MISMATCH");
    boolean sigOk=verifySignature(bundle); if(!sigOk)add(errors,"SIGNATURE_INVALID");
    boolean reqOk=true; for(String section:REQUIRED) if(!ev.has(section)){ reqOk=false; add(errors,"MISSING_SECTION:"+section); }
    boolean cross=reqOk;
    if(reqOk){
      ObjectNode tr=o(ev,"transaction_record"), p=o(ev,"asset_provenance"), r=o(ev,"rights"), l=o(ev,"licence"), u=o(ev,"usage_receipt");
      ObjectNode st=o(ev,"license_settlement"), vr=o(ev,"value_record"), d=o(ev,"digital_commodity"), ca=o(ev,"corporate_authorization"), c=o(ev,"capital"), ss=o(ev,"share_settlement");
      if(!eq(p,"asset_id",tr,"asset_id")){cross=false;add(errors,"PROVENANCE_ASSET_MISMATCH");}
      if(!eq(r,"asset_id",tr,"asset_id")){cross=false;add(errors,"RIGHTS_ASSET_MISMATCH");}
      if(!eq(r,"claimant_entity_id",l,"grantor_entity_id")){cross=false;add(errors,"RIGHTS_CLAIMANT_MISMATCH");}
      if(!eq(l,"asset_id",tr,"asset_id")||!eq(l,"grantor_entity_id",tr,"grantor_entity_id")||!eq(l,"licensee_entity_id",tr,"licensee_entity_id")||!eq(l,"rights_claim_id",r,"claim_id")){cross=false;add(errors,"LICENCE_LINK_MISMATCH");}
      if(!eq(u,"licence_id",l,"licence_id")||!eq(u,"asset_id",tr,"asset_id")||!eq(u,"user_entity_id",l,"licensee_entity_id")){cross=false;add(errors,"USAGE_LINK_MISMATCH");}
      if(!s(u,"purpose").equals(s(l,"authorized_purpose"))){cross=false;add(errors,"USAGE_PURPOSE_UNAUTHORIZED");}
      if(!eq(st,"licence_id",l,"licence_id")){cross=false;add(errors,"SETTLEMENT_LINK_MISMATCH");}
      if(!eq(st,"payer_entity_id",l,"licensee_entity_id")||!eq(st,"payee_entity_id",l,"grantor_entity_id")){cross=false;add(errors,"SETTLEMENT_DIRECTION_INVALID");}
      if(b(vr,"realized_external")&&(!eq(vr,"settlement_id",st,"settlement_id")||!b(st,"verified_external")||i(vr,"amount_minor")>i(st,"amount_minor"))){cross=false;add(errors,"VALUE_EXCEEDS_SETTLEMENT");}
      if(!eq(d,"asset_id",tr,"asset_id")||!eq(d,"usage_id",u,"usage_id")||!eq(d,"licence_id",l,"licence_id")||!eq(d,"settlement_id",st,"settlement_id")){cross=false;add(errors,"COMMODITY_LINK_MISMATCH");}
      if(i(d,"contribution_minor")>i(st,"amount_minor")){cross=false;add(errors,"COMMODITY_EXCEEDS_SETTLEMENT");}
      if(!eq(ca,"share_class_id",c,"share_class_id")||!eq(ca,"issuance_request_id",c,"issuance_request_id")){cross=false;add(errors,"CAPITAL_AUTH_MISMATCH");}
      ObjectNode accounting=o(c,"accounting"); if(i(accounting,"debit_minor")!=i(accounting,"credit_minor")){cross=false;add(errors,"CAPITAL_ACCOUNTING_UNBALANCED");}
      long pos=0; for(JsonNode x:a(c,"positions")) pos+=i(x,"shares");
      if(i(c,"outstanding_before")+i(c,"shares_issued")!=i(c,"outstanding_after")||pos!=i(c,"outstanding_after")){cross=false;add(errors,"CAPITAL_SHARES_UNRECONCILED");}
      if(!eq(ss,"capital_event_id",c,"capital_event_id")){cross=false;add(errors,"SHARE_SETTLEMENT_LINK_MISMATCH");}
    }
    boolean ledgerOk=reqOk&&verifyLedger(o(ev,"event_ledger"),errors); Collections.sort(errors);
    boolean overall=rootOk&&sigOk&&reqOk&&cross&&ledgerOk&&errors.isEmpty();
    ObjectNode out=M.createObjectNode(); out.put("schema","entity-cleanroom-verification-result-v1"); out.put("transaction_id",s(bundle,"transaction_id"));
    out.put("transaction_root_sha256",s(bundle,"transaction_root_sha256")); out.put("root_valid",rootOk); out.put("signature_valid",sigOk); out.put("required_sections_valid",reqOk);
    out.put("cross_links_valid",cross); out.put("ledger_valid",ledgerOk); out.put("overall_valid",overall); ArrayNode ec=out.putArray("error_codes"); errors.forEach(ec::add); return out;
  }
  static String resultHash(ObjectNode result) throws Exception { return sha(canonical(result)); }
  static ObjectNode verifyRecovery(Path dir,Path keyFile) throws Exception {
    ObjectNode m=(ObjectNode)load(dir.resolve("RECOVERY_MANIFEST.json")); byte[] bundle=Files.readAllBytes(dir.resolve("TRANSACTION_BUNDLE.json")); byte[] enc=Files.readAllBytes(dir.resolve("STATE_BACKUP.enc")); byte[] key=hex(Files.readString(keyFile).trim());
    ObjectNode unsigned=m.deepCopy(); unsigned.remove("signature"); ObjectNode sg=o(m,"signature"); boolean sigOk=false;
    try{
      PublicKey pub=KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(s(sg,"public_key_spki_der_b64"))));
      Signature v=Signature.getInstance("Ed25519"); v.initVerify(pub); v.update(canonical(unsigned).getBytes(StandardCharsets.UTF_8)); sigOk=v.verify(Base64.getDecoder().decode(s(sg,"sig_b64")));
    }catch(Exception ignored){}
    boolean hashes=sha(bundle).equals(s(m,"transaction_bundle_sha256"))&&sha(enc).equals(s(m,"encrypted_state_sha256"))&&sha(key).equals(s(m,"recovery_key_fingerprint_sha256"));
    boolean dec=false; String restored="";
    try{
      byte[] nonce=Base64.getDecoder().decode(s(m,"nonce_b64")); Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec((int)i(m,"tag_bytes")*8,nonce)); byte[] plain=c.doFinal(enc);
      JsonNode state=M.readTree(plain); restored=sha(canonical(state.get("evidence"))); dec=true;
    }catch(Exception ignored){}
    boolean ok=sigOk&&hashes&&dec&&restored.equals(s(m,"transaction_root_sha256")); ObjectNode out=M.createObjectNode();
    out.put("schema","entity-cleanroom-recovery-result-v1"); out.put("signature_valid",sigOk); out.put("hashes_valid",hashes); out.put("decrypt_valid",dec);
    out.put("restored_transaction_root_sha256",restored); out.put("expected_transaction_root_sha256",s(m,"transaction_root_sha256")); out.put("overall_valid",ok); return out;
  }

  static boolean hex64(String x){ return x.matches("[0-9a-f]{64}"); }
  static List<String> strings(ArrayNode a){ List<String> z=new ArrayList<>(); for(JsonNode n:a) if(n.isTextual())z.add(n.asText()); return z; }
  static boolean sortedUnique(ArrayNode a){ List<String> x=strings(a), y=new ArrayList<>(new TreeSet<>(x)); return x.size()==a.size()&&x.equals(y); }
  static boolean validGlobal(ObjectNode r){
    return switch(s(r,"schema")){
      case "entity-v3-jurisdiction-profile-v1" -> b(r,"legal_effect_is_deployment_specific") && a(r,"rules").size()>0 && allRules(a(r,"rules"));
      case "entity-v3-semantic-term-v1" -> KINDS.contains(s(r,"kind")) && hex64(s(r,"definition_sha256")) && "ACTIVE".equals(s(r,"status"));
      case "entity-v3-topology-node-v1" -> TOPOLOGY.contains(s(r,"topology_class")) && b(r,"infrastructure_membership_is_not_sovereign_authority");
      case "entity-v3-purpose-bound-access-v1" -> a(r,"purposes").size()>0 && a(r,"actions").size()>0 && i(r,"max_uses")>=0;
      case "entity-v3-offline-envelope-v1" -> hex64(s(r,"payload_sha256")) && i(r,"sequence")>=0 && i(r,"expires_at_ms")>i(r,"created_at_ms");
      case "entity-v3-crypto-transition-v1" -> b(r,"downgrade_after_transition_prohibited") && i(r,"old_retire_at_ms")>=i(r,"dual_sign_from_ms");
      case "entity-v3-data-economic-capital-v1" -> b(r,"information_bytes_are_not_declared_scarce") && hex64(s(r,"provenance_root")) && hex64(s(r,"content_sha256"));
      case "entity-v3-bounded-economic-interest-v1" -> validInterest(r);
      default -> false;
    };
  }
  static boolean allRules(ArrayNode rules){
    for(JsonNode q:rules){ String effect=s(q,"effect"); if(!Set.of("ALLOW","REQUIRE","PROHIBIT").contains(effect)||a(q,"actions").isEmpty())return false; } return true;
  }
  static boolean validInterest(ObjectNode r){
    ArrayNode actions=a(r,"actions"), ss=a(r,"scarcity_sources"); List<String> scarcity=strings(ss); long p=i(r,"participation_bps");
    return !actions.isEmpty()&&sortedUnique(actions)&&scarcity.contains("RIGHT")&&scarcity.stream().allMatch(SCARCITY::contains)&&b(r,"underlying_information_remains_nonrival")&&p>=0&&p<=10000;
  }
  static boolean verifyChecksums(Path root) throws Exception {
    String text=Files.readString(root.resolve("SHA256SUMS.txt"),StandardCharsets.UTF_8).replace("\uFEFF","").replace("\r","");
    for(String line:text.split("\n")){ if(line.isBlank())continue; int at=line.indexOf("  "); if(at<1)return false; String expected=line.substring(0,at).trim(), rel=line.substring(at+2).trim();
      Path p=root.resolve(rel); if(!Files.exists(p)||!sha(Files.readAllBytes(p)).equals(expected))return false; }
    return true;
  }
  static ObjectNode runGlobal(Path root) throws Exception {
    boolean checksums=verifyChecksums(root); ObjectNode profile=(ObjectNode)load(root.resolve("ENTITY_GLOBAL_CLEANROOM_PROFILE.json")); ObjectNode manifest=(ObjectNode)load(root.resolve("vectors/VECTOR_MANIFEST.json"));
    ArrayNode rows=M.createArrayNode(); int passed=0, valid=0;
    for(JsonNode ee:a(manifest,"vectors")){
      String file=s(ee,"file"); ObjectNode payload=(ObjectNode)load(root.resolve("vectors").resolve(file)); boolean accepted=validGlobal((ObjectNode)payload.get("record")); String expected=s(payload,"expect"); boolean ok=accepted=="VALID".equals(expected);
      if(ok)passed++; if(accepted)valid++; ObjectNode row=M.createObjectNode(); row.put("name",file.substring(0,file.length()-5)); row.put("accepted",accepted); row.put("expected",expected); row.put("ok",ok); rows.add(row);
    }
    List<JsonNode> sorted=new ArrayList<>(); rows.forEach(sorted::add); sorted.sort(Comparator.comparing(x->s(x,"name"))); ArrayNode ordered=M.createArrayNode(); sorted.forEach(ordered::add);
    ObjectNode summary=M.createObjectNode(); summary.put("schema","entity-v3.1-global-cleanroom-result-v1"); summary.put("profile","ENTITY-GLOBAL-INFRASTRUCTURE"); summary.set("doctrine_invariants",profile.get("doctrine_invariants")); summary.set("vectors",ordered);
    String result=sha(canonical(summary)); boolean overall=checksums&&passed==ordered.size()&&result.equals(s(profile,"expected_result_sha256"))&&valid==i(profile,"valid_vectors")&&(ordered.size()-valid)==i(profile,"invalid_vectors");
    ObjectNode out=M.createObjectNode(); out.put("implementation","java"); out.put("checksums_pass",checksums); out.put("vectors_passed",passed); out.put("vectors_total",ordered.size()); out.put("result_sha256",result); out.put("expected_result_sha256",s(profile,"expected_result_sha256"));
    out.set("doctrine_invariants",profile.get("doctrine_invariants")); out.put("overall_valid",overall); out.set("results",ordered); return out;
  }

  static boolean errorCodesEqual(JsonNode actual,JsonNode expected){ return actual!=null&&expected!=null&&actual.equals(expected); }
  static ObjectNode runLegacy(Path kit) throws Exception {
    if(!verifyChecksums(kit)) throw new IllegalStateException("legacy sealed-kit checksum failure");
    ObjectNode manifest=(ObjectNode)load(kit.resolve("vectors/VECTOR_MANIFEST.json")); ArrayNode rows=M.createArrayNode(); int passed=0;
    for(JsonNode vv:a(manifest,"vectors")){
      ObjectNode result=verifyBundle((ObjectNode)load(kit.resolve("vectors").resolve(s(vv,"file")))); ObjectNode exp=o(vv,"expected"); boolean ok=b(result,"overall_valid")==b(exp,"overall_valid")&&errorCodesEqual(result.get("error_codes"),exp.get("error_codes")); if(ok)passed++;
      ObjectNode row=M.createObjectNode(); row.put("name",s(vv,"name")); row.put("ok",ok); row.put("result_sha256",resultHash(result)); row.set("result",result); rows.add(row);
    }
    ObjectNode recovery=verifyRecovery(kit.resolve("vectors/recovery"),kit.resolve("vectors/test_inputs/recovery_key.hex")); ObjectNode out=M.createObjectNode(); out.put("implementation","java"); out.put("vectors_passed",passed); out.put("vectors_total",a(manifest,"vectors").size()); out.put("recovery_pass",b(recovery,"overall_valid")); out.put("golden_root",s(manifest,"valid_transaction_root_sha256")); out.set("results",rows); out.set("recovery",recovery); return out;
  }
  public static void main(String[] args) throws Exception {
    Path root=Paths.get("").toAbsolutePath();
    if(args.length>0&&!"test".equals(args[0])){
      ObjectNode r=verifyBundle((ObjectNode)load(Paths.get(args[0]))); ObjectNode out=M.createObjectNode(); out.put("result_sha256",resultHash(r)); out.set("result",r); System.out.println(M.writeValueAsString(out)); if(!b(r,"overall_valid"))System.exit(1); return;
    }
    ObjectNode legacy=runLegacy(root.resolve("conformance-kit")); ObjectNode global=runGlobal(root.resolve("global-conformance-kit"));
    ObjectNode report=M.createObjectNode(); report.put("implementation","java"); report.set("legacy_v3",legacy); report.set("global_v3_1",global);
    boolean ok=i(legacy,"vectors_passed")==i(legacy,"vectors_total")&&b(legacy,"recovery_pass")&&b(global,"overall_valid"); report.put("overall_valid",ok);
    System.out.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(report)); if(!ok)System.exit(1);
  }
}
