# ENTITY-JAVA-CLEANROOM

[![Clean-room verification](https://github.com/blackmore-technology-group/ENTITY-JAVA-CLEANROOM/actions/workflows/cleanroom-verify.yml/badge.svg)](https://github.com/blackmore-technology-group/ENTITY-JAVA-CLEANROOM/actions/workflows/cleanroom-verify.yml)
[![License](https://img.shields.io/github/license/blackmore-technology-group/ENTITY-JAVA-CLEANROOM)](LICENSE)

**Document class:** BTG-controlled reproducibility baseline  
**Frozen campaign target:** ENTITY v3.4.2 Global Passport  
**Current supported ENTITY runtime:** v3.4.3  
**BTDU component in v3.4.3:** Blackmore Technology Data Universe (BTDU) 3.4.2 unchanged

> This repository is controlled by Blackmore Technology Group Limited (BTG). It is reproducibility evidence, **not** an unrelated third-party implementation or independent external validation.

[ENTITY](https://github.com/blackmore-technology-group/ENTITY) · [Current v3.4.3 release](https://github.com/blackmore-technology-group/ENTITY/releases/tag/v3.4.3) · [Documentation model](https://blackmore-technology-group.github.io/ENTITY-DOCS/reference/documentation-model.html) · [External verification challenge](https://github.com/blackmore-technology-group/ENTITY/issues/55)

## Frozen v3.4.2 campaign

This Java baseline retains the exact v3.4.2 Global Passport campaign:

- sealed vectors: **26/26 PASS**;
- sealed-kit SHA-256: `ced70113f1d153627eb972b11adbf20e502ed086e0b13e8abf1dc5adc4c2e716`;
- canonical campaign result SHA-256: `45af773554a7191c1b49a75c636a1106afb1de36d788bb00d7af56097b8d1b0e`;
- required `overall_valid: true`.

The version label identifies the frozen evidence target and does not state that v3.4.2 is the current runtime.

### Windows PowerShell

An external reproducer identified that unquoted Maven `-D...` properties can be parsed incorrectly by PowerShell. Use:

```powershell
mvn -q "-Dstyle.color=never" "-DskipTests" compile
mvn -q "-Dstyle.color=never" exec:java "-Dexec.mainClass=org.btg.entity.cleanroom.PassportV34"
```

### Bash / compatible shells

```bash
mvn -q -Dstyle.color=never -DskipTests compile
mvn -q -Dstyle.color=never exec:java -Dexec.mainClass=org.btg.entity.cleanroom.PassportV34
```

## Other controlled campaigns

The repository also retains earlier controlled campaigns and their historical material. See [`.github/workflows/cleanroom-verify.yml`](.github/workflows/cleanroom-verify.yml) for the exact CI commands and evidence capture.

## Where this fits now

ENTITY v3.4.3 is the current supported runtime. BTDU remains component 3.4.2 unchanged. Protocol 1.0 is a separate frozen external clean-room target; BTDU, ADAM and NIKI are not additional Protocol 1.0 requirements unless the sealed Protocol 1.0 material explicitly says so.

## Verification boundary

A PASS here shows that this **BTG-controlled Java baseline** matches the frozen v3.4.2 target. External reproduction of this baseline is meaningful portability/reproducibility evidence, but it is not the same as an independently designed Protocol 1.0 implementation or full external interoperability/recovery.

It does not by itself establish objective external truth, legal title, regulatory compliance, accounting fair value, upstream ownership or automatic economic entitlement.

## Contributing

Reproducible failures, portability fixes, language-idiomatic improvements, test corrections, specification ambiguities and counterexamples are useful. Independent conformance evidence should live in a repository controlled outside BTG.

## License

Apache License 2.0. See [LICENSE](LICENSE).
