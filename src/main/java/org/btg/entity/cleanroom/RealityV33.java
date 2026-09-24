package org.btg.entity.cleanroom;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class RealityV33 {
  static final ObjectMapper M=new ObjectMapper();
  static final Path KIT=Path.of("reality-conformance-kit/ENTITY_V3_3_REALITY_CLEANROOM_KIT.min.json");
  static final String KIT_SHA="e0d6ba26baa405557bc2990e39d3022ebb8cda00ae797fab0100773cf304a6fd";
  static final String EXPECTED="82bd1f1fb328edd37a26d8ea60ede5a599c7d9af5027bffd73b9e52843b5a51d";
  static final List<String> P=List.of("ENTITY","AUTHORITY","RIGHT","EVENT","VALUE");
  static final Set<String> STATES=Set.of("OBSERVED","ASSERTED","INFERRED","ATTESTED","EXTERNALLY_VERIFIED","ADJUDICATED","DISPUTED","REVOKED","UNKNOWN");
  static final Set<String> EVIDENCE=Set.of("SENSOR_OBSERVATION","DOCUMENT","REGISTRY_RECORD","LAB_RESULT","PAYMENT_RECORD","IMAGE","API_RESPONSE","CERTIFICATE","COURT_RECORD","OTHER");
  static final Set<String> ANCHORS=Set.of("GOVERNMENT_REGISTRY","SENSOR_NETWORK","BANK_SETTLEMENT","LAB_SYSTEM","SUPPLY_CHAIN_SYSTEM","CORPORATE_REGISTRY","COURT_RECORD","CERTIFICATE_AUTHORITY","OTHER");
  static final Set<String> NODES=Set.of("SOURCE_DATA","DCO","RIGHT","LICENSE","USAGE","DERIVED_ASSET","PRODUCT","TRANSACTION","REVENUE","SETTLEMENT","CONTRIBUTOR");
  static final Set<String> EDGES=Set.of("ORIGINATED_FROM","AUTHORIZED_BY","LICENSED_AS","USED_IN","DERIVED_FROM","PRODUCED","GENERATED","SETTLED_AS","CONTRIBUTED_TO");
  static String sha(byte[] b)throws Exception{byte[] h=MessageDigest.getInstance("SHA-256").digest(b);return HexFormat.of().formatHex(h);}
  static String s(JsonNode r,String k){JsonNode x=r.get(k);return x!=null&&x.isTextual()?x.asText():"";}
  static boolean b(JsonNode r,String k,boolean w){JsonNode x=r.get(k);return x!=null&&x.isBoolean()&&x.asBoolean()==w;}
  static boolean hex64(JsonNode x){return x!=null&&x.isTextual()&&x.asText().matches("[0-9a-f]{64}");}
  static List<String> strings(JsonNode x){if(x==null||!x.isArray())return null;List<String> z=new ArrayList<>();for(JsonNode q:x){if(!q.isTextual()||q.asText().isEmpty())return null;z.add(q.asText());}return z;}
  static boolean refs(JsonNode x,boolean nonempty){List<String> z=strings(x);if(z==null||nonempty&&z.isEmpty())return false;List<String> sorted=new ArrayList<>(new TreeSet<>(z));return z.equals(sorted);}
  static boolean arrEq(JsonNode x,List<String>w){List<String> z=strings(x);return z!=null&&z.equals(w);}
  static boolean valid(JsonNode r){return switch(s(r,"schema")){
    case "entity-v3-evidence-object-v1" -> EVIDENCE.contains(s(r,"evidence_type"))&&hex64(r.get("content_sha256"))&&b(r,"signature_proves_attribution_not_objective_truth",true)&&b(r,"immutable_evidence_record",true);
    case "entity-v3-evidence-bound-claim-v1" -> STATES.contains(s(r,"state"))&&hex64(r.get("value_sha256"))&&refs(r.get("evidence_refs"),false)&&b(r,"claim_is_not_objective_truth",true)&&b(r,"state_is_typed_not_absolute",true);
    case "entity-v3-claim-status-transition-v1" -> STATES.contains(s(r,"from_state"))&&STATES.contains(s(r,"to_state"))&&!s(r,"from_state").equals(s(r,"to_state"))&&refs(r.get("evidence_refs"),false)&&b(r,"history_rewrite_prohibited",true)&&b(r,"transition_does_not_establish_objective_truth",true);
    case "entity-v3-attestation-authority-grant-v1" -> refs(r.get("scopes"),true)&&hex64(r.get("authority_evidence_sha256"))&&b(r,"attestation_authority_is_scope_limited",true)&&b(r,"attestation_does_not_create_legal_truth",true);
    case "entity-v3-attestation-v1" -> !s(r,"grant_id").isEmpty()&&!s(r,"scope").isEmpty()&&refs(r.get("evidence_refs"),true)&&b(r,"attestation_is_evidence_not_objective_truth",true);
    case "entity-v3-external-reality-anchor-v1" -> ANCHORS.contains(s(r,"anchor_type"))&&hex64(r.get("endpoint_descriptor_sha256"))&&b(r,"credentials_included",false)&&b(r,"external_system_is_not_automatic_entity_authority",true);
    case "entity-v3-external-reality-snapshot-v1" -> hex64(r.get("record_sha256"))&&refs(r.get("verifier_evidence_refs"),false)&&b(r,"external_record_is_evidence_not_protocol_truth",true)&&b(r,"record_may_be_contested_or_superseded",true);
    case "entity-v3-causal-economic-node-v1" -> {JsonNode o=r.get("economic_observation");boolean obsok=o==null||!o.isObject()||o.isEmpty()||(b(o,"market_observation_is_not_accounting_fair_value",true)&&b(o,"protocol_does_not_determine_legal_entitlement",true));yield NODES.contains(s(r,"node_type"))&&refs(r.get("evidence_refs"),false)&&refs(r.get("event_refs"),false)&&obsok;}
    case "entity-v3-causal-economic-edge-v1" -> EDGES.contains(s(r,"edge_type"))&&!s(r,"from_node_id").equals(s(r,"to_node_id"))&&refs(r.get("evidence_refs"),true)&&refs(r.get("authority_refs"),false)&&refs(r.get("participation_rule_refs"),false)&&b(r,"causality_is_evidence_bound_not_assumed",true)&&b(r,"economic_attribution_is_not_accounting_fair_value",true);
    case "entity-v3-verifiable-reality-status-v1" -> arrEq(r.get("core_primitives"),P)&&b(r,"core_semantics_changed",false)&&b(r,"market_engine_preserved",true)&&b(r,"reality_claims_are_evidence_bound",true)&&b(r,"cryptographic_verification_is_not_objective_truth",true)&&b(r,"protocol_verification_is_not_objective_truth",true);
    default -> false;};}
  static String canonical(JsonNode n)throws Exception{if(n.isObject()){List<String>ks=new ArrayList<>();n.fieldNames().forEachRemaining(ks::add);Collections.sort(ks);List<String>parts=new ArrayList<>();for(String k:ks)parts.add(M.writeValueAsString(k)+":"+canonical(n.get(k)));return "{"+String.join(",",parts)+"}";}if(n.isArray()){List<String>parts=new ArrayList<>();for(JsonNode x:n)parts.add(canonical(x));return "["+String.join(",",parts)+"]";}return M.writeValueAsString(n);}
  public static void main(String[]args)throws Exception{byte[]raw=Files.readAllBytes(KIT);if(!sha(raw).equals(KIT_SHA))throw new IllegalStateException("sealed v3.3 kit SHA-256 mismatch");JsonNode kit=M.readTree(raw);List<JsonNode>cases=new ArrayList<>();kit.path("cases").forEach(cases::add);cases.sort(Comparator.comparing(x->x.path("id").asText()));ArrayNode transcript=M.createArrayNode();int passed=0;for(JsonNode c:cases){String actual=valid(c.path("record"))?"VALID":"INVALID";if(actual.equals(s(c,"expect")))passed++;ObjectNode row=M.createObjectNode();row.put("id",s(c,"id"));row.put("actual",actual);transcript.add(row);}String result=sha(canonical(transcript).getBytes(StandardCharsets.UTF_8));boolean overall=passed==20&&result.equals(EXPECTED)&&s(kit,"expected_result_sha256").equals(EXPECTED);ObjectNode out=M.createObjectNode();out.put("implementation","java");out.put("kit_sha256",KIT_SHA);out.put("vectors_passed",passed);out.put("vectors_total",20);out.put("result_sha256",result);out.put("expected_result_sha256",EXPECTED);out.put("overall_valid",overall);System.out.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(out));if(!overall)System.exit(1);}
}
