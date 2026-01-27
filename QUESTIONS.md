# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I have already added some comments directly in the code regarding this topic.
In general, I tried not to change the interface signatures too much, although I still adjusted them slightly where it improved clarity and consistency.

If I were maintaining this codebase, I would prefer to query the database using the required filtering criteria directly, rather than loading broader datasets and applying filtering via stream iterations in memory. This helps both performance and clarity of intent.

I would also avoid returning null where possible and consistently use Optional instead, as it makes the API contracts clearer and reduces the risk of NullPointerExceptions.

Additionally, I would avoid mixing service layers. If WarehouseResourceImpl already relies on operation/service classes that encapsulate repository access, then WarehouseResourceImpl itself should not call the repository directly. Keeping this separation makes responsibilities clearer, improves testability, and reduces coupling between layers.

Moreover, instead of call to LegacyStoreManagerGateway after a successful transaction I would send an asynchronous message there. I believe, the legacy system should be located in a separated whatever microservice or system.  

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (YAML → generated code)
Pros:
- Single source of truth for the contract: request/response schemas, status codes, error models, security, etc.
- Stronger consistency across teams (backend, frontend, QA) and easier collaboration.
- Easier client generation (typed SDKs) and automated documentation.
- Makes it harder to accidentally introduce breaking changes, especially if you add contract validation / linting in CI.

Cons:
- Can add workflow overhead: you have to maintain the spec and generation pipeline.
- Generated code can be verbose and sometimes awkward to customize, especially if generation templates aren’t tuned.
- If the team is not disciplined, the spec can drift into “documentation only” and not reflect reality unless you validate it.
- For small/simple endpoints it can feel like too much ceremony.

Code-first (implement endpoints directly)
Pros:
- Fast iteration: fewer steps, easier to prototype and change behavior quickly.
- Less tooling complexity (no generator, fewer build steps).
- You can use idiomatic framework patterns without fighting the generated structure.

Cons:
- Higher risk of inconsistent API behavior across services (status codes, error shapes, naming).
- Documentation may become out of date unless you add something like OpenAPI generation from annotations.
- Harder to reliably support client generation and contract-driven testing.

What I would choose
If this is a production system with multiple consumers (frontend, other services, external clients), I would prefer a consistent approach, ideally:
- OpenAPI-first for all public-facing APIs (Warehouse, Product, Store), or at least for the ones used by other teams/services.

If the team values rapid iteration and the APIs are internal-only, I’d still keep a contract, but I might go code-first with OpenAPI generated from code (annotations), so we still get:
- accurate docs
- client generation
- contract testing

The key point for me is consistency across modules: mixing approaches can create uneven quality and maintenance cost. I’d pick one strategy as the default and only deviate when there’s a clear reason (e.g., a small internal endpoint or a prototype).

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would primarily prioritize integration tests that cover the full flow
(REST → service → repository → database), because in practice integration tests tend to reveal the majority of real issues during development—especially problems related to configuration, transactions, mappings, and data access.

Whenever possible, I would prefer black-box tests without mocks or stubs, as they are the closest to real system behavior and give the highest level of confidence that the application works correctly as a whole.

At the same time, since integration tests are usually more time- and resource-consuming, I would also rely on fast unit tests for business and service-layer logic, where it is sufficient to verify different behavioral scenarios of a class in isolation. This helps keep feedback cycles short while still maintaining confidence in the core logic.

I would set a mandatory test coverage threshold of around 80%, as this level typically provides good protection without significantly increasing development complexity or slowing the team down.

For object mapping, I would use MapStruct to generate mapper implementations. In that case, I would exclude mapper implementation classes from test coverage, as well as simple data classes such as entities or DTOs, since they usually contain no business logic and provide little value when tested directly.
```