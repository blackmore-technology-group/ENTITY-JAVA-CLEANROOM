package org.btg.entity.cleanroom;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class AdoptionV32 {
  static final ObjectMapper M=new ObjectMapper();
  static final Path KIT=Path.of("adoption-conformance-kit/ENTITY_V3_2_ADOPTION_CLEANROOM_KIT.min.json");
  static final String KIT_SHA="44e7a00f910c89aced3b3c1b5e9cba486809ca313e9bfb4b7bc9266095c10c14";
  static final String EXPECTED="1eb59e09ab08da86bfd8584df4a64ba331f7bbbce3d236b9b94f351606c90e18";
  static final List<String> PRIMITIVES=List.of("ENTITY","AUTHORITY","RIGHT","EVENT","VALUE");
  static final List<String> LIFECYCLE=List.of("DCO","INSTRUMENT","LISTING","DISCLOSURE","ORDER_RFQ_AUCTION","PRICE_DISCOVERY","TRADE","CLEARING","SETTLEMENT","ENTITLEMENT","USAGE","DERIVED_OUTPUT","ECONOMIC_CONSEQUENCE");
  static final Set<String> PROVIDERS=Set.of("AWS_S3","AZURE_BLOB","GOOGLE_CLOUD_STORAGE","SNOWFLAKE","DATABRICKS","POSTGRESQL","SQL_SERVER","LOCAL_FILESYSTEM","HTTP_API");
  static final Set<String> STANDARDS=Set.of("ODRL","W3C_VC","DID","GAIA_X","IDS");
  static String sha(byte[] b)throws Exception{byte[] h=MessageDigest.getInstance("SHA-256").digest(b);return HexFormat.of().formatHex(h);}
  static String s(JsonNode r,String k){JsonNode x=r.get(k);return x!=null&&x.isTextual()?x.asText():"";}
  static boolean b(JsonNode r,String k,boolean w){JsonNode x=r.get(k);return x!=null&&x.isBoolean()&&x.asBoolean()==w;}
  static boolean hex64(JsonNode x){return x!=null&&x.isTextual()&&x.asText().matches("[0-9a-f]{64}");}
  static List<String> strings(JsonNode x){if(x==null||!x.isArray())return null;List<String> z=new ArrayList<>();for(JsonNode q:x){if(!q.isTextual())return null;z.add(q.asText());}return z;}
  static boolean arrEq(JsonNode x,List<String>w){List<String> z=strings(x);return z!=null&&z.equals(w);}
  static boolean rules(JsonNode x){if(x==null||!x.isArray()||x.isEmpty())return false;for(JsonNode q:x){String e=s(q,"effect");if(!Set.of("ALLOW","REQUIRE","PROHIBIT").contains(e))return false;List<String>a=strings(q.get("actions"));if(a==null||a.isEmpty())return false;List<String>sorted=new ArrayList<>(new TreeSet<>(a));if(!a.equals(sorted))return false;for(String v:a)if(v.isEmpty()||!v.equals(v.toUpperCase(Locale.ROOT)))return false;}return true;}
  static boolean valid(JsonNode r){return switch(s(r,"schema")){
    case "entity-v3-rights-passport-v1" -> arrEq(r.get("core_primitives"),PRIMITIVES)&&rules(r.get("rights"))&&b(r,"provider_custody_is_not_authority",true)&&b(r,"underlying_data_not_silently_transferred",true)&&b(r,"legal_effect_is_deployment_specific",true);
    case "entity-v3-custody-locator-v1" -> PROVIDERS.contains(s(r,"provider"))&&hex64(r.get("content_sha256"))&&b(r,"provider_is_authority",false)&&b(r,"credentials_included",false)&&b(r,"entity_identity_changes_with_provider",false);
    case "entity-v3-standards-mapping-v1" -> STANDARDS.contains(s(r,"source_standard"))&&hex64(r.get("source_sha256"))&&b(r,"silent_semantic_equivalence",false)&&b(r,"external_standard_is_not_entity_authority",true);
    case "entity-v3-external-credential-evidence-v1" -> s(r,"source_standard").equals("W3C_VC")&&hex64(r.get("credential_sha256"))&&b(r,"credential_is_evidence_not_entity_authority",true);
    case "entity-v3-resolver-deployment-v1" -> s(r,"mode").equals("FEDERATED")&&r.path("minimum_resolvers").isInt()&&r.path("minimum_resolvers").asInt()>=2&&b(r,"resolver_is_not_authority",true)&&b(r,"single_provider_dependency_prohibited",true)&&b(r,"fail_closed",true);
    case "entity-v3-exchange-adoption-profile-v1" -> b(r,"market_engine_preserved",true)&&b(r,"rights_are_traded_not_bytes",true)&&arrEq(r.get("market_lifecycle"),LIFECYCLE);
    case "entity-v3-adoption-profile-status-v1" -> arrEq(r.get("core_primitives"),PRIMITIVES)&&b(r,"core_semantics_changed",false)&&b(r,"market_engine_preserved",true);
    case "entity-v3-legal-classification-assertion-v1" -> !s(r,"asserted_by").isEmpty()&&!s(r,"classification").isEmpty()&&b(r,"classification_is_assertion_not_protocol_legal_truth",true);
    default -> false;};}
  static String canonical(JsonNode n)throws Exception{if(n.isObject()){List<String>ks=new ArrayList<>();n.fieldNames().forEachRemaining(ks::add);Collections.sort(ks);List<String>parts=new ArrayList<>();for(String k:ks)parts.add(M.writeValueAsString(k)+":"+canonical(n.get(k)));return "{"+String.join(",",parts)+"}";}if(n.isArray()){List<String>parts=new ArrayList<>();for(JsonNode x:n)parts.add(canonical(x));return "["+String.join(",",parts)+"]";}return M.writeValueAsString(n);}
  public static void main(String[]args)throws Exception{byte[]raw=Files.readAllBytes(KIT);if(!sha(raw).equals(KIT_SHA))throw new IllegalStateException("sealed kit SHA-256 mismatch");JsonNode kit=M.readTree(raw);ArrayNode rows=M.createArrayNode();int passed=0;for(JsonNode v:kit.path("vectors")){boolean accepted=valid(v.path("record"));String exp=s(v,"expect");boolean ok=accepted==(exp.equals("VALID"));if(ok)passed++;ObjectNode row=M.createObjectNode();row.put("name",s(v,"name"));row.put("accepted",accepted);row.put("expected",exp);row.put("ok",ok);rows.add(row);}List<JsonNode>list=new ArrayList<>();rows.forEach(list::add);list.sort(Comparator.comparing(x->x.path("name").asText()));ArrayNode sorted=M.createArrayNode();list.forEach(sorted::add);ObjectNode summary=M.createObjectNode();summary.put("schema","entity-v3.2-adoption-cleanroom-result-v1");summary.put("profile","ENTITY-ADOPTION-LAYER");summary.set("adoption_invariants",kit.path("profile").path("adoption_invariants"));summary.set("vectors",sorted);String result=sha(canonical(summary).getBytes(StandardCharsets.UTF_8));boolean overall=passed==16&&result.equals(EXPECTED);ObjectNode out=M.createObjectNode();out.put("implementation","java");out.put("kit_sha256",KIT_SHA);out.put("vectors_passed",passed);out.put("vectors_total",16);out.put("result_sha256",result);out.put("expected_result_sha256",EXPECTED);out.put("overall_valid",overall);System.out.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(out));if(!overall)System.exit(1);}
}
