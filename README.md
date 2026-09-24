# ENTITY Java Clean-room

Independent native Java implementation used for BTG-controlled ENTITY conformance qualification.

This repository intentionally does **not** import, translate, call, link, or inspect the BTG Python reference implementation. It consumes sealed public conformance inputs and implements verification semantics in Java.

## Qualified campaigns

1. Legacy ENTITY transaction/recovery clean-room campaign:
   - 10 valid/invalid transaction vectors
   - Ed25519 signature verification
   - provenance/rights/licence/usage/settlement/capital cross-link verification
   - event-ledger continuity
   - AES-256-GCM sovereign recovery
2. ENTITY v3.1 global-infrastructure / Data Economic Sovereignty campaign:
   - 16 vectors total: 8 valid + 8 invalid
   - jurisdiction profile
   - semantic registry term
   - topology/offline envelope
   - purpose-bound access
   - cryptographic transition
   - data-economic capital and bounded economic interest

Expected v3.1 result SHA-256:

`879c8e1eba2549a9a1962605760b715b0fd47d3ea640fb9c6f29be5c63cfb8c5`

This is BTG-controlled clean-room evidence, not unrelated third-party external verification.
