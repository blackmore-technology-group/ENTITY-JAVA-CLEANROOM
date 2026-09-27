# ENTITY-JAVA-CLEANROOM

[![Clean-room verification](https://github.com/blackmore-technology-group/ENTITY-JAVA-CLEANROOM/actions/workflows/cleanroom-verify.yml/badge.svg)](https://github.com/blackmore-technology-group/ENTITY-JAVA-CLEANROOM/actions/workflows/cleanroom-verify.yml)
[![License](https://img.shields.io/github/license/blackmore-technology-group/ENTITY-JAVA-CLEANROOM)](LICENSE)

**BTG-controlled Java conformance baseline for ENTITY v3.4.2.**

> This repository is maintained and controlled by Blackmore Technology Group. It is cross-language reproducibility evidence. It is **not** an unrelated third-party implementation and must not be cited as independent external validation.

[ENTITY](https://github.com/blackmore-technology-group/ENTITY) · [v3.4.2 release](https://github.com/blackmore-technology-group/ENTITY/releases/tag/v3.4.2) · [Documentation portal](https://blackmore-technology-group.github.io/ENTITY-DOCS/) · [External verification challenge](https://github.com/blackmore-technology-group/ENTITY/issues/55)

## What this repository verifies

This Java implementation exercises published ENTITY conformance campaigns, including the frozen/earlier clean-room baseline, v3.2 adoption, v3.3 Verifiable Reality and the **v3.4.2 Global Passport campaign**.

CI verifies the campaign inputs and executes the language-native classifiers while retaining verification evidence as workflow artifacts.

## ENTITY v3.4.2 Global Passport campaign

The current Java CI target requires:

- sealed vectors: **26/26 PASS**;
- canonical campaign result SHA-256: `45af773554a7191c1b49a75c636a1106afb1de36d788bb00d7af56097b8d1b0e`;
- `overall_valid: true`.

The v3.4.2-specific sealed kit used by the language baselines is committed with SHA-256:

`ced70113f1d153627eb972b11adbf20e502ed086e0b13e8abf1dc5adc4c2e716`

Run the current Java campaign:

```bash
mvn -q -Dstyle.color=never -DskipTests compile
mvn -q -Dstyle.color=never exec:java -Dexec.mainClass=org.btg.entity.cleanroom.PassportV34
```

The repository also retains historical v3.4 material as provenance/reproducibility evidence. That historical material should not be confused with the current 26-vector v3.4.2 Java campaign target.

## Other controlled campaigns

```bash
mvn -q -Dstyle.color=never exec:java -Dexec.mainClass=org.btg.entity.cleanroom.Main -Dexec.args=test
mvn -q -Dstyle.color=never exec:java -Dexec.mainClass=org.btg.entity.cleanroom.AdoptionV32
mvn -q -Dstyle.color=never exec:java -Dexec.mainClass=org.btg.entity.cleanroom.RealityV33
```

See [`.github/workflows/cleanroom-verify.yml`](.github/workflows/cleanroom-verify.yml) for the current CI procedure and evidence capture.

## Where this fits in ENTITY

ENTITY v3.4.2 adds the Blackmore Technology Data Universe (BTDU) while preserving the Global Passport/profile architecture and the core primitives:

`ENTITY → AUTHORITY → RIGHT → EVENT → VALUE`

The architecture preserves identity, authority, rights, evidence, provenance and portable economic state without treating infrastructure possession as sovereign authority.

If you are evaluating ENTITY rather than this Java baseline specifically, start at the [ENTITY repository](https://github.com/blackmore-technology-group/ENTITY), the [documentation portal](https://blackmore-technology-group.github.io/ENTITY-DOCS/), or the [external verification challenge](https://github.com/blackmore-technology-group/ENTITY/issues/55).

## Verification boundary

A passing result shows that this **BTG-controlled Java implementation** classifies its current sealed campaign consistently with the published v3.4.2 target.

It does **not** establish unrelated third-party validation, objective external truth, regulatory compliance, legal title, accounting fair value or independent live interoperability merely because another BTG-controlled language converges on the same result.

The stronger external question remains open:

> Can an unrelated engineer or organization reproduce ENTITY semantics from public specifications and sealed test material without using BTG implementation code?

## Contributing

Useful contributions include reproducible build failures, portability fixes, language-idiomatic improvements, test corrections, specification ambiguities and independently authored counterexamples.

If your goal is to produce **independent** conformance evidence, use a repository controlled outside BTG and follow the public verification challenge's independence rules.

## License

Apache License 2.0. See [LICENSE](LICENSE).
