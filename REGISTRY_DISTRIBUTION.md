# Maven distribution boundary

The Maven artifact packages the existing BTG-controlled Java v3.4.2 Global Passport verifier for easier discovery and execution. It remains BTG-controlled reproducibility/conformance evidence and must not be described as independent third-party validation.

The sealed v3.4.2 kit is packaged into the runnable JAR and its SHA-256 is still verified before classification. Registry packaging must not alter the expected 26-vector classifications, sealed-kit hash, canonical result hash, or fail-closed behavior.

Maven Central publication additionally requires authorized namespace/account/signing setup; repository readiness does not imply that publication has occurred.
