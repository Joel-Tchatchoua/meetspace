# 🏦 KORA CORE

**Product**: Neobank wallet core, Mobile Money and card-ready.

**Scope**: P2P, cash-in/out, merchant payment, multi-provider orchestration, deferred settlement, reconciliation, risk, observability, cloud orchestration and WalletOps.

> **The numbering in this file is authoritative.** `CLAUDE.md` refers to these steps and numbers none of its own. See `ADR-007`.
>
> **Historical status**: Steps 0, 1 and 3 were delivered before this extended roadmap. Step 3 was delivered before Step 2. Step 2 remains open until modular boundaries are enforced structurally and behaviorally, not only by folder layout.

---

## 🎯 North Star

KORA Core must evolve through a strict dependency chain:

```text
Financial correctness
    ↓
Customer identity and trust
    ↓
Modular business architecture
    ↓
Distributed consistency
    ↓
Provider orchestration
    ↓
Settlement and reconciliation
    ↓
Real-time risk intelligence
    ↓
Cloud-native operations
    ↓
WalletOps and controlled AI
```

The governing rule is:

> AI may observe, retrieve, correlate, explain and propose. Deterministic Spring services authenticate, authorize, validate financial invariants and execute financial operations.

---

# 🧱 STEP 0 — Conscious transactional monolith

**Status**: Delivered, then continuously hardened.

## 🎯 Business context

Launch the first financially correct wallet core:

- customer and account creation;
- cash-in;
- cash-out;
- P2P transfer;
- immutable accounting history;
- complete auditability.

## ✅ Business value

Without financial correctness, support, disputes, reconciliation, audits and customer trust become fragile. This step establishes the minimum credible foundation for a Djamo, Wave, Orange Money or M-Pesa-type wallet.

## 📊 Initial volume targets

- 5,000 transactions/day;
- 5 to 10 requests/second at peak;
- P95 below 150 ms for core commands;
- database load approximately 30 to 60 QPS;
- targets are simulation objectives, not claimed production metrics.

## 🔐 Security baseline

- stable `customerId` as primary identity;
- verified phone number as mutable alias, never as immutable identity;
- short-lived access tokens;
- refresh-token rotation and replay detection;
- strict validation and injection protection;
- secrets outside source code;
- no PII or secrets in logs;
- OWASP baseline;
- audit events for sensitive operations.

## 🔧 Technical work

- immutable double-entry ledger;
- no direct wallet balance update;
- balance derived from postings, with controlled projections or snapshots;
- amounts represented in minor units;
- currency explicit on every monetary object;
- database constraints for accounting integrity;
- indexes on account, transaction, journal and idempotency identifiers;
- ACID transaction boundaries;
- idempotency keys for retriable commands;
- financial invariant and concurrency tests.

## 🧠 Questions you must answer

- Why must a financial balance not be treated as an ordinary mutable field?
- How is a balance reconstructed from journal entries?
- What happens if the same transfer command is submitted ten times?
- Where is the concurrency boundary when two transactions spend the same available funds?
- What is recorded when a business operation fails after partial internal work?

## 🚪 Exit gate

- for every posted transaction, total debits equal total credits;
- replaying the same command produces one business effect;
- all wallet positions can be reconstructed;
- concurrent spending tests cannot generate a negative available balance unless the product explicitly allows credit;
- historical entries are reversed, never silently mutated.

## 🧩 Skill acquired

Financially correct transactional backend engineering.

---

# 💰 STEP 1 — Money correctness and wallet lifecycle

**Status**: Delivered, then continuously extended.

## 🎯 Business context

A wallet balance is not enough. Real payment systems distinguish authorization, reservation, posting, settlement and reversal.

## 🔧 Technical work

Model explicitly:

- ledger balance;
- available balance;
- pending balance;
- holds and reservations;
- fees;
- FX amounts and rates;
- refunds;
- reversals;
- chargebacks;
- expiration;
- value date versus processing date;
- suspense positions.

Implement a transition-controlled lifecycle:

```text
INITIATED → VALIDATED → AUTHORIZED → CAPTURED → SETTLED
      │           │           │            │
      └──────────→ FAILED      └──────────→ REVERSED
                  │
                  └──────────→ EXPIRED / CANCELLED
```

## Failure cases

- hold expires while capture is in progress;
- duplicate capture;
- provider timeout after successful submission;
- late success after internal failure;
- late reversal;
- partial refund greater than remaining captured amount;
- rounding mismatch;
- duplicate callback;
- out-of-order callback.

## 🚪 Exit gate

- lifecycle transitions reject illegal movement;
- holds prevent overspending;
- capture and release are idempotent;
- refunds and reversals preserve accounting balance;
- property-based tests cover money invariants and edge cases.

## 🧩 Skill acquired

Money correctness beyond CRUD and simple balance transfer.

---

# 🧩 STEP 2 — Enforced modular monolith

**Status**: Open until all gates below are satisfied.

## 🎯 Business context

The system must scale in complexity before it scales into services. A folder split is not a modular monolith.

## Target modules

```text
identity
customer
kyc
wallet
ledger
payment
risk
provider
settlement
reconciliation
notification
audit
```

## Boundary rules

- each module owns its domain model and persistence;
- another module cannot access internal repositories directly;
- cross-module communication uses a public module API, an application port or an internal event;
- shared domain models are forbidden by default;
- transactions remain local and explicit;
- dependencies follow an approved direction;
- ArchUnit or equivalent tests enforce boundaries;
- schemas, events and public contracts are versioned;
- module ownership and confidentiality constraints are documented.

## 🚪 Exit gate

- forbidden dependencies fail the build;
- module APIs are intentionally small;
- no cross-module repository access exists;
- a module can be isolated or extracted without rewriting the entire codebase;
- the dependency graph matches documented architecture decisions.

## 🧩 Skill acquired

Modular architecture with enforceable boundaries.

---

# 🏗️ STEP 3 — Hexagonal architecture and domain isolation

**Status**: Delivered, then enforced across new modules.

## Target structure

```text
domain
application
ports/in
ports/out
adapters/in
adapters/out
bootstrap
```

## Rules

- inbound ports are owned by the application use cases they expose;
- the consuming module defines the outbound port it needs;
- infrastructure adapters implement outbound ports;
- Spring, JPA, Kafka, HTTP and provider SDKs stay outside the domain;
- domain tests run without infrastructure;
- mapping between domain and persistence/API models is explicit;
- transactions are started at the appropriate application boundary.

## 🚪 Exit gate

A transfer, hold, capture, reversal and ledger posting can be unit-tested entirely in memory using substituted ports.

## 🧩 Skill acquired

Domain-centric architecture resilient to framework and infrastructure changes.

---

# 🪪 STEP 4 — Customer identity, PIN, devices and recoverability

## 🎯 Business context

A wallet user changes phone numbers, loses devices, resets PINs and may suffer SIM swap or account takeover. Authentication must model that reality.

## Lifecycle

```text
Registration
→ Phone verification
→ PIN definition
→ Device enrollment
→ Login
→ Step-up authentication
→ Session management
→ Logout or revocation
→ Lost-device recovery
→ Account recovery
→ Device replacement
```

## Technical work

- stable customer identity separate from phone number;
- verified phone aliases with history and change controls;
- PIN protected using an appropriate password-hashing strategy and throttling;
- device-bound asymmetric credentials where supported;
- server challenge signed by enrolled device key;
- access-token short lifetime;
- refresh-token rotation, family tracking and replay detection;
- session and device revocation;
- step-up requirements based on risk and operation value;
- secure PIN reset and phone-number change;
- recovery that does not bypass KYC;
- immutable audit of enrollment, revocation and recovery events.

## Threats

- SIM swap;
- credential stuffing;
- PIN guessing;
- stolen refresh token;
- cloned device state;
- malicious device enrollment;
- recovery-channel takeover;
- session fixation and replay.

## 🚪 Exit gate

Demonstrate device revocation, refresh-token replay detection, safe device replacement, PIN reset, phone-number change and recovery with complete audit evidence.

## 🧩 Skill acquired

Wallet-grade identity and device security.

---

# 🛂 STEP 5 — Progressive KYC, compliance and customer risk

## 🎯 Business context

KYC is not an onboarding checkbox. Customer capability changes with identity evidence, geography, risk and regulation.

## Capabilities

- progressive KYC tiers;
- identity document capture;
- proof of address where required;
- liveness or biometric provider integration when justified;
- sanctions and PEP screening;
- manual review;
- document expiration;
- re-verification;
- continuous KYC;
- customer risk tier;
- wallet restriction or freeze;
- configurable transaction and balance limits;
- versioned policies by jurisdiction and legal entity;
- evidence retention and decision audit.

## Policy model

```text
KYC Tier 0 → minimal capabilities
KYC Tier 1 → low wallet and P2P limits
KYC Tier 2 → increased limits and products
EDD        → enhanced review for higher-risk profiles
```

BEAC, BCEAO and other jurisdictional controls must be versioned policies, not hard-coded universal rules.

## 🚪 Exit gate

- customer capabilities are derived from current KYC and risk state;
- expired or revoked evidence changes capabilities predictably;
- policy decisions are reproducible for a historical date and version;
- manual-review outcomes are attributable;
- no autonomous AI decision approves ambiguous KYC evidence.

## 🧩 Skill acquired

Regulation-aware customer lifecycle and policy engineering.

---

# 📤 STEP 6 — Transactional Outbox and event contracts

## 🎯 Business context

A committed financial operation and its integration event must not diverge because of a dual write.

## Flow

```text
SQL transaction
├── business state
├── ledger postings
└── outbox record
       ↓
Publisher
       ↓
Kafka
       ↓
Idempotent consumers
```

## Technical work

- transactional outbox;
- stable event identity;
- aggregate identity and sequence;
- correlation and causation identifiers;
- schema registry;
- compatibility policy;
- at-least-once delivery;
- idempotent consumers;
- retry with bounded backoff and jitter;
- controlled DLQ;
- replay procedure;
- poison-message handling;
- outbox backlog monitoring;
- consumer-lag monitoring;
- partition-key decisions based on ordering requirements.

## Principle

```text
Exactly-once transport semantics
≠ exactly-once business effect
```

Business correctness comes from idempotence, invariants and durable state.

## 🚪 Exit gate

Kill the application at each point between commit, publication and consumption. Recover without losing an event or producing a duplicate business effect.

## 🧩 Skill acquired

Event-driven consistency under at-least-once delivery.

---

# 🌐 STEP 7 — Multi-provider payment orchestration

## 🎯 Business context

Mobile Money, bank and card providers have inconsistent APIs, timeouts, callbacks and settlement models. Provider details must not leak into the financial core.

## Architecture

```text
Canonical payment command
→ provider router
→ provider adapter
→ external provider
→ normalized provider result
```

## Technical work

- canonical provider contract;
- routing by capability, corridor, cost and health;
- timeout and deadline propagation;
- circuit breaker;
- bulkhead;
- retry with jitter only when safe;
- provider-specific idempotency;
- callback signature verification;
- duplicate and out-of-order callback handling;
- ambiguous-outcome state;
- provider health score;
- controlled failover;
- manual intervention workflow.

## Critical distinction

- failure before submission;
- failure during submission;
- timeout after provider acceptance;
- accepted but callback missing;
- callback duplicated;
- success arriving after internal timeout;
- provider state conflicting with KORA state.

Blind retry after an unknown outcome is forbidden.

## 🚪 Exit gate

Provider simulations prove safe behavior for timeout, duplicate callback, late success, conflicting status, circuit opening and controlled failover.

## 🧩 Skill acquired

Resilient payment-provider integration.

---

# 🧾 STEP 8 — Settlement and reconciliation

## 🎯 Business context

An API success is not final settlement. Multiple ledgers and reports diverge in real payment systems.

## End-to-end chain

```text
Initiation
→ Validation
→ Authorization
→ Routing
→ Clearing
→ Settlement
→ Ledger posting
→ Reconciliation
```

## Technical work

- settlement position model;
- import and normalization of provider reports;
- bank-statement ingestion;
- matching engine;
- configurable tolerances;
- missing and duplicate transaction detection;
- amount, fee and currency mismatch detection;
- late-settlement handling;
- returned-payment handling;
- suspense accounts;
- manual investigation queue;
- maker-checker correction workflow;
- auditable adjustment postings;
- reconciliation metrics and aging.

## 🚪 Exit gate

Reconcile KORA ledger, provider report, bank statement and settlement position. Every break has a type, owner, evidence, aging and auditable resolution.

## 🧩 Skill acquired

Financial finality and reconciliation engineering.

---

# 🛡️ STEP 9 — Real-time risk and fraud intelligence

## 🎯 Business context

Rules must catch obvious abuse immediately. Streaming and analytical signals must identify account takeover, mule networks and coordinated behavior.

## Deterministic controls

- per-transaction limits;
- rolling daily and weekly limits;
- velocity rules;
- blocklists and allowlists;
- beneficiary cooling period;
- failed-login counters;
- device binding;
- geo-velocity;
- corridor restrictions;
- KYC-tier limits.

## Streaming layer

```text
Kafka events
→ Kafka Streams
→ windows and state stores
→ real-time features
→ risk decision service
```

## Scenarios

- account takeover;
- SIM swap;
- mule accounts;
- device farms;
- rapid cash-in/cash-out;
- circular fund movement;
- shared-device rings;
- one-to-many and many-to-one bursts;
- impossible travel;
- structuring below thresholds.

## Decision contract

```text
ALLOW
CHALLENGE
REVIEW
BLOCK
```

ML may contribute a score. A deterministic policy service owns the executable decision.

## 🚪 Exit gate

- decisions are reproducible from evidence and policy version;
- late and out-of-order events are handled;
- false-positive and false-negative trade-offs are documented;
- overrides are audited;
- no model writes directly to wallet or ledger state.

## 🧩 Skill acquired

Real-time financial risk engineering.

---

# 📊 STEP 10 — Data platform, ML and graph analytics

## Dependency rule

Do not build this before domain events, labels and business definitions are stable.

## Architecture

```text
Operational events
→ governed data contracts
→ streaming ingestion
→ analytical storage
→ feature pipelines
→ online/offline features
→ training
→ registry
→ serving
→ monitoring
```

## Technical work

- event taxonomy and ownership;
- data contracts;
- analytical storage model;
- point-in-time-correct features;
- training-serving consistency;
- feature registry;
- model registry;
- champion/challenger deployment;
- drift and quality monitoring;
- graph-based entity resolution;
- relationship and community signals;
- explainability;
- privacy and retention controls.

## 🚪 Exit gate

A versioned risk model can be trained, evaluated, deployed and rolled back while preserving feature lineage and reproducible decisions.

## 🧩 Skill acquired

Production ML integrated into financial workflows.

---

# ☁️ STEP 11 — Cloud platform, security and supply chain

## Progression

```text
Containers
→ CI/CD
→ supply-chain controls
→ cloud foundations
→ Terraform
→ Kubernetes
→ policy enforcement
→ progressive delivery
```

## Pipeline gates

- compilation;
- unit and property tests;
- ArchUnit tests;
- integration tests with Testcontainers;
- contract tests;
- financial invariant tests;
- concurrency and recovery tests;
- SAST;
- dependency and licence scanning;
- secret scanning;
- SBOM generation;
- container scanning;
- IaC scanning;
- signed images and attestations;
- deployment approval by risk tier;
- smoke tests;
- rollback verification.

## Platform controls

- workload identity;
- encrypted secrets;
- network policies;
- pod security controls;
- resource limits;
- horizontal scaling with domain-aware limits;
- database connection budgeting;
- environment separation;
- backup and restore;
- disaster recovery.

## 🚪 Exit gate

A release is blocked by failing financial, security, architecture or supply-chain controls, and the platform can restore service and data within documented recovery objectives.

## 🧩 Skill acquired

Secure cloud-native delivery of critical financial services.

---

# 📈 STEP 12 — Observability, resilience and SRE

## SLIs and SLOs

Technical:

- latency, traffic, errors and saturation;
- database lock waits and connection pressure;
- Kafka lag;
- outbox age and backlog;
- retry and DLQ volume;
- provider availability.

Business:

- transfer success rate;
- duplicate-effect rate;
- unresolved reconciliation breaks;
- settlement delay;
- risk-review aging;
- wrongful decline rate;
- recovery completion rate.

## Technical work

- OpenTelemetry traces;
- structured logs without PII;
- metric cardinality control;
- correlation and causation propagation;
- actionable alerts;
- runbooks;
- error budgets;
- game days;
- dependency failure injection;
- backup restoration drills;
- region and provider outage simulations.

## 🚪 Exit gate

The team can diagnose a failed or delayed payment from customer request through ledger, outbox, Kafka, provider, settlement and reconciliation using one correlated evidence chain.

## 🧩 Skill acquired

Operational ownership of financial infrastructure.

---

# 🤖 STEP 13 — WalletOps AI Control Plane

## 🎯 Business context

Operations teams investigate inconsistent payments across APIs, ledger entries, events, provider callbacks, risk decisions and settlement files. WalletOps reduces investigation time without bypassing financial controls.

## Core workflow

```text
Operator request
→ transaction lookup
→ timeline reconstruction
→ ledger verification
→ provider-status retrieval
→ outbox/Kafka inspection
→ risk-context retrieval
→ settlement evidence
→ root-cause hypotheses
→ remediation proposal
→ human approval when required
→ deterministic domain command
→ post-action verification
→ immutable audit
```

## 13.1 Document Intelligence for KYC

- extract structured fields from simulated KYC documents;
- schema validation;
- field-level confidence;
- field-level evaluation;
- manual review for ambiguity;
- no autonomous approval of uncertain identity evidence.

## 13.2 Regulatory RAG

- authoritative and versioned sources;
- legal entity and jurisdiction metadata;
- effective date;
- access control;
- citations required;
- refusal when evidence is insufficient;
- indirect prompt-injection protection.

## 13.3 Safe Text2SQL

Only against:

- read-only views;
- an analytical replica;
- allowlisted schemas;
- bounded execution time;
- limited rows;
- complete query audit.

Generated SQL must never mutate the ledger or bypass domain APIs.

## 13.4 Persistent investigation workflow

- explicit state machine;
- durable checkpoints;
- bounded steps;
- token and cost budgets;
- timeout and cancellation;
- deterministic fallback;
- human approval;
- restart after failure;
- kill switch.

## 13.5 MCP tool gateway

Read tools:

```text
getTransaction
getTransactionTimeline
getLedgerEvidence
getProviderStatus
getRiskContext
getSettlementEvidence
```

Controlled-write tools:

```text
proposeRemediation
requestApproval
openOperationsCase
retrySafeProviderQuery
republishApprovedOutboxEvent
executeApprovedRemediation
```

Every tool requires:

- typed schema;
- narrow purpose;
- authenticated caller;
- least privilege;
- independent argument validation;
- timeout and rate limit;
- idempotency when applicable;
- complete audit;
- sensitive-data filtering.

## 13.6 AgentOps

- versioned prompts, tools and configuration;
- golden, edge and adversarial datasets;
- retrieval evaluation;
- tool-selection evaluation;
- authorization tests;
- ship/no-ship threshold;
- complete tracing;
- replay;
- per-user state isolation;
- model and prompt rollback;
- cost per investigation;
- escalation and human-override rates;
- quality monitoring without relying only on HTTP errors.

## 🚪 Exit gate

WalletOps must demonstrate:

- evidence-backed investigation;
- no direct model access to transactional persistence;
- no financial action without deterministic validation;
- explicit approval for consequential remediation;
- full decision and tool lineage;
- replayable incidents;
- safe fallback when model or retrieval is unavailable;
- measurable reduction in investigation time without degraded financial correctness.

## 🧩 Skill acquired

Enterprise AI integrated safely into regulated financial operations.

---

# 🧪 Cross-cutting proof matrix

| Dimension | Minimum proof |
|---|---|
| Accounting | Property tests proving debit equals credit |
| Idempotency | Repeated commands produce one business effect |
| Concurrency | Race-condition suite for spending, holds and capture |
| Recovery | Crash testing around commit, publication and consumption |
| Architecture | Automated module and dependency rules |
| Security | Threat model, authorization tests and secrets controls |
| Events | Schema compatibility, replay and idempotent consumers |
| Providers | Timeout, duplicate callback and unknown-outcome simulations |
| Reconciliation | Break detection and auditable correction |
| Risk | Versioned policy and reproducible decisions |
| Platform | SBOM, scans, signed artifact and rollback |
| SRE | Traces, SLOs, runbooks and game-day evidence |
| AI | Evals, approval gates, traceability, rollback and kill switch |

---

# 🧭 Implementation order

```text
1. Finish and enforce STEP 2
2. Harden STEP 0 and STEP 1 invariants
3. Apply STEP 3 consistently
4. Build identity and recovery
5. Add progressive KYC and policies
6. Implement Outbox and event contracts
7. Add provider orchestration
8. Build settlement and reconciliation
9. Introduce streaming risk controls
10. Build the analytical and ML foundation
11. Industrialize cloud, CI/CD and supply chain
12. Establish SRE and recovery practices
13. Add WalletOps incrementally, starting read-only
```

WalletOps autonomy must progress as follows:

```text
Observe
→ Explain
→ Recommend
→ Draft an action
→ Execute after approval
→ Execute low-risk bounded actions only after evidence
```

---

# ✅ Definition of final maturity

KORA Core is mature when it can demonstrate all of the following:

- financial positions are reconstructible and balanced;
- retries cannot create duplicate financial effects;
- concurrent operations preserve available funds;
- identity, devices, sessions and recovery are auditable;
- KYC policy is versioned and jurisdiction-aware;
- event publication survives process failure;
- provider ambiguity is modelled explicitly;
- settlement and reconciliation expose real finality;
- real-time risk decisions are evidence-backed;
- releases contain software-supply-chain evidence;
- incidents are diagnosable end to end;
- WalletOps accelerates operations without obtaining uncontrolled financial authority.

---

# 📌 Final positioning

```text
KORA Core
=
Financially correct wallet core
+ regulated identity and KYC
+ resilient distributed payment infrastructure
+ settlement and reconciliation
+ real-time risk intelligence
+ secure cloud-native operations
+ AI-assisted WalletOps control plane
```

This roadmap intentionally produces a profile at the intersection of:

- senior Java and Spring backend engineering;
- distributed systems;
- financial infrastructure and money correctness;
- event streaming and Kafka;
- cloud and platform engineering;
- applied AI in real production.
