package org.btg.entity.cleanroom;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class PassportV34 {
  static final String KIT="passport-conformance-kit-v341/ENTITY_V3_4_GLOBAL_PASSPORT_CLEANROOM_KIT.min.json";
  static final String KIT_SHA="f95c2b347da97742fed3f20611f0eec2fd3df48694fed9494fb07163c537cfb7";
  static final String EXPECTED="ac7504cce70576008cff069607619660a4b9bf0cad43b3f3de81078f1e80d9ba";
  static final List<String> CORE=List.of("ENTITY","AUTHORITY","RIGHT","EVENT","VALUE");
  static final Set<String> KINDS=Set.of("GLOBAL","JURISDICTION","INDUSTRY","DOMAIN","PRIVACY","TRUST","DISCLOSURE");
  static final ObjectMapper M=new ObjectMapper();
  static String sha(byte[] b)throws Exception{byte[] h=MessageDigest.getInstance("SHA-256").digest(b);return HexFormat.of().formatHex(h);}
  static String s(JsonNode r,String k){JsonNode v=r.get(k);return v!=null&&v.isTextual()?v.asText():"";}
  static boolean b(JsonNode r,String k,boolean w){JsonNode v=r.get(k);return v!=null&&v.isBoolean()&&v.asBoolean()==w;}
  static boolean hex64(JsonNode v){return v!=null&&v.isTextual()&&v.asText().matches("[0-9a-f]{64}");}
  static boolean arrEq(JsonNode v,List<String>w){if(v==null||!v.isArray()||v.size()!=w.size())return false;for(int i=0;i<w.size();i++)if(!w.get(i).equals(v.get(i).asText()))return false;return true;}
  static boolean stack(JsonNode r){JsonNode refs=r.get("profile_refs"),hs=r.get("profile_hashes");if(refs==null||!refs.isArray()||refs.isEmpty()||hs==null||!hs.isArray()||refs.size()!=hs.size())return false;boolean global=false;for(JsonNode x:refs)if("entity-profile:global@1.0".equals(x.asText()))global=true;for(JsonNode x:hs)if(!hex64(x))return false;return global&&b(r,"fail_closed",true)&&b(r,"profile_composition_does_not_create_authority",true)&&b(r,"standards_mapping_is_not_normative_equivalence",true);}
  static boolean valid(JsonNode r){return switch(s(r,"schema")){
    case "entity-v3-global-passport-profile-status-v1" -> arrEq(r.get("core_primitives"),CORE)&&b(r,"core_semantics_changed",false)&&b(r,"market_engine_preserved",true)&&b(r,"one_passport_many_profiles",true)&&b(r,"evidence_truth_boundary_preserved",true);
    case "entity-v3-global-profile-v1" -> !s(r,"profile_ref").isEmpty()&&KINDS.contains(s(r,"kind"))&&hex64(r.get("schema_sha256"))&&b(r,"profile_is_not_authority",true)&&b(r,"standards_mapping_is_not_normative_equivalence",true)&&(!s(r,"profile_id").toUpperCase(Locale.ROOT).contains("DEFENCE")||b(r,"public_unclassified",true));
    case "entity-v3-profile-stack-resolution-v1" -> stack(r);
    case "entity-v3-global-passport-v1" -> passport(r);
    case "entity-v3-continuous-ingest-result-v1" -> r.path("files").canConvertToInt()&&r.path("files").asInt()>=0&&hex64(r.get("inventory_sha256"))&&b(r,"content_addressed",true)&&b(r,"custody_is_not_authority",true)&&b(r,"economic_value_invented",false);
    default -> false;};}
  static boolean passport(JsonNode r){JsonNode e=r.get("economic_state"),ms=r.get("standards_mappings");if(e==null||!e.isObject()||!e.path("amount_units").canConvertToLong()||e.path("amount_units").asLong()<0||!b(e,"market_observation_is_not_accounting_fair_value",true)||ms==null||!ms.isArray())return false;for(JsonNode m:ms)if(!b(m,"normative_equivalence_claimed",false))return false;return arrEq(r.get("core_primitives"),CORE)&&!s(r,"rights_passport_id").isEmpty()&&hex64(r.get("rights_passport_sha256"))&&stack(r.get("profile_stack"))&&b(r,"one_passport_many_profiles",true)&&b(r,"profile_composition_does_not_create_authority",true)&&b(r,"standards_mapping_is_not_normative_equivalence",true)&&b(r,"evidence_does_not_establish_objective_truth",true)&&b(r,"legal_effect_is_deployment_specific",true)&&b(r,"underlying_information_remains_nonrival",true);}
  static String canon(JsonNode n)throws Exception{if(n.isObject()){List<String>ks=new ArrayList<>();n.fieldNames().forEachRemaining(ks::add);Collections.sort(ks);List<String>p=new ArrayList<>();for(String k:ks)p.add(M.writeValueAsString(k)+":"+canon(n.get(k)));return"{"+String.join(",",p)+"}";}if(n.isArray()){List<String>p=new ArrayList<>();for(JsonNode x:n)p.add(canon(x));return"["+String.join(",",p)+"]";}return M.writeValueAsString(n);}
  public static void main(String[] args)throws Exception{byte[]raw=Files.readAllBytes(Path.of(KIT));if(!sha(raw).equals(KIT_SHA))throw new IllegalStateException("sealed kit SHA-256 mismatch");JsonNode kit=M.readTree(raw);List<JsonNode>cases=new ArrayList<>();kit.get("cases").forEach(cases::add);cases.sort(Comparator.comparing(x->s(x,"id")));ArrayNode rows=M.createArrayNode();int passed=0;for(JsonNode c:cases){String actual=valid(c.get("record"))?"VALID":"INVALID";if(actual.equals(s(c,"expect")))passed++;ObjectNode row=M.createObjectNode();row.put("id",s(c,"id"));row.put("actual",actual);rows.add(row);}String result=sha(canon(rows).getBytes(StandardCharsets.UTF_8));boolean overall=passed==24&&result.equals(EXPECTED)&&s(kit,"expected_result_sha256").equals(EXPECTED);ObjectNode out=M.createObjectNode();out.put("implementation","java");out.put("kit_sha256",KIT_SHA);out.put("vectors_passed",passed);out.put("vectors_total",24);out.put("result_sha256",result);out.put("expected_result_sha256",EXPECTED);out.put("overall_valid",overall);System.out.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(out));if(!overall)System.exit(1);}
}
