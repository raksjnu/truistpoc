graph TD
    %% Start
    Start([Developer Commits Code / System Update]) --> Phase1
    
    %% Phase 1
    subgraph Phase 1: Detection & Gating
        Phase1[GitLab CI/CD Pipeline] --> S1[SonarQube: Code Quality]
        Phase1 --> V1[Veracode: SAST / SCA]
        Phase1 --> Q1[Qualys: Infrastructure]
        
        S1 --> Gate{Critical Flaw?}
        V1 --> Gate
        Q1 --> Gate
        
        Gate -- Yes --> Block[Block Build / Fail Pipeline]
        Gate -- No --> Phase2
    end
    
    %% Phase 2
    subgraph Phase 2: Triage & Risk
        Phase2((Central Security Hub)) --> T1[Deduplicate Findings]
        T1 --> T2[Apply Contextual Risk Prioritization]
    end
    
    %% Phase 3
    subgraph Phase 3: The Remediation Factory
        T2 --> Split{Vulnerability Type}
        Split -- Open Source / Dependencies --> FixA[Auto-Generate GitLab Merge Request]
        Split -- Code / SSL / Infrastructure --> FixB[Auto-Generate ServiceNow Ticket]
    end
    
    %% Phase 4
    subgraph Phase 4: Action
        FixA --> Ops[Operations Support Team]
        FixB --> Ops
        Ops --> Deploy[Review, Test, and Deploy Secure Build]
    end
    
    %% Phase 5
    subgraph Phase 5: Governance & Maintenance
        Deploy --> Dash1[Splunk: Security Events]
        Deploy --> Dash2[Dynatrace: Real-time MTTR & SLAs]
    end
    
    %% Maintenance Loop
    Dash1 -.-> |Continuous Monitoring Loop| Start
    Dash2 -.-> |Continuous Monitoring Loop| Start
