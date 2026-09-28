# Maven Central / Central Portal publication

The Java repository is now repository-ready as a runnable distribution artifact:

- Maven artifact version is aligned to the frozen v3.4.2 campaign;
- normal project/license/developer/SCM metadata is present;
- the sealed kit is packaged as a classpath resource;
- the runnable shaded JAR verifies the exact sealed-kit SHA-256 before classification;
- package smoke executes the JAR and requires 26/26, the expected canonical result hash and `overall_valid: true`.

## Remaining external publication steps

Maven Central publication requires registry/account work that should not be bypassed by committing reusable secrets here. Before publication:

1. create/confirm the Central Portal publishing account;
2. verify an appropriate namespace for Blackmore Technology Group;
3. configure the signing method and publication metadata required by Central;
4. confirm current Central Portal account/tier/publishing requirements at the time of release;
5. run the existing clean-room verification and registry package smoke on the exact source to be released;
6. publish only from a deliberately versioned release source;
7. record the resulting Central coordinates and immutable source commit after publication.

Do not store a long-lived Central Portal credential or private signing key in the repository merely to automate the first release.

## Evidence boundary

Maven Central publication would make the BTG-controlled Java verifier easier to discover and execute. It would not constitute unrelated third-party validation, independent protocol implementation, or full ENTITY v3.4.3 qualification.
