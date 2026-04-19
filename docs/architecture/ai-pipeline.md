# AI Pipeline Notes

## 1. Role Of AI In FitCart AI

AI in this project is used for four main jobs:

- explain nutrients and supplement ingredients
- summarize review corpora
- support recommendation answers
- summarize uploaded document content safely

The design goal is:

> use AI where language understanding helps, but keep structured data and business rules outside the prompt

That is the difference between a credible AI product and a fake AI wrapper.

## 2. Grounded AI Pattern

FitCart AI follows a grounded AI pattern:

1. structured data is gathered first
2. relevant evidence is selected
3. the AI service receives constrained input
4. the output is validated against schemas
5. the system returns educational or recommendation support, not unconstrained free-form claims

This matters because it reduces:

- hallucinations
- overclaiming
- prompt drift
- inconsistent explanations

## 3. Feature-Level Pipelines

### Nutrient Explanations

Input:

- nutrient name
- user question
- structured metadata:
  - aliases
  - nutrient type
  - short description
  - possible benefits
  - product categories
  - warnings
  - beginner relevance

Pipeline:

1. backend prepares nutrient metadata
2. FastAPI receives the grounded request
3. explanation logic generates a constrained educational response
4. response is schema-validated
5. response can be cached

### Review Summaries

Input:

- product name
- category
- review list

Pipeline:

1. reviews are normalized
2. recurring themes are extracted
3. grounded summary output is generated:
   - pros
   - cons
   - common complaints
   - who it is good for
   - who should avoid it
4. output is cached for repeated reads

### Recommendations

Input:

- user query
- user preferences
- product catalog
- review analytics
- semantic signals

Pipeline:

1. query is parsed
2. candidates are retrieved
3. ranking is computed in Spring Boot
4. a grounded recommendation answer is composed
5. if higher-level AI-style flow degrades, structured catalog fallback is returned

### Document Summaries

Input:

- uploaded file metadata
- extracted text

Pipeline:

1. Spring Boot manages upload metadata and lifecycle
2. extraction result is sent to the AI service
3. AI service produces:
   - simplified summary
   - important terms explained
   - supplement/nutrition-relevant observations
   - safe disclaimer

Important boundary:

- the system explains
- it does not diagnose
- it does not prescribe treatment

## 4. Retrieval And RAG Thinking

Not all data should be embedded.

Good retrieval candidates:

- product descriptions
- educational ingredient notes
- review summaries
- buying-guide content

Keep structured:

- price
- brand
- category
- protein grams
- sugar grams
- dietary flags
- rating

This leads to a hybrid architecture:

- structured filters narrow exact constraints
- semantic retrieval improves meaning-based discovery
- ranking combines both

## 5. Safety Philosophy

Because the product touches health-adjacent information, the system should default to:

- educational framing
- evidence-bounded language
- clear disclaimers
- avoidance of diagnosis and dosage advice

Safety is not just a policy issue. It is an architecture issue.

The safest architecture is one where:

- structured facts are explicit
- AI sees constrained input
- output formats are schema-based
- fallback paths exist when AI is unavailable

## 6. Evaluation Mindset

A strong AI pipeline also needs evaluation.

The most important evaluation dimensions here are:

- grounding
- relevance
- factual faithfulness to source input
- clarity
- safety

This is why FitCart AI is a good learning project: it lets you discuss both system design and AI evaluation, not just prompt engineering.
