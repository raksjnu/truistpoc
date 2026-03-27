```mermaid
graph TD
    %% Level 1
    subgraph Level 1: Auto Detection
        direction TB
        L1_Start([Code Commit / Build]) --> L1_SQ[SonarQube: Quality Scan]
        L1_SQ --> L1_Vera[Veracode: SAST / SCA]
        L1_Vera --> L1_Infra[Container / Infra Scan]
    end

    %% Level 2
    subgraph Level 2: Auto Prioritization
        L1_Infra --> L2_Engine{Central Risk Engine}
        L2_Engine -->|Analyzes CVSS, Exposure, Data Sensitivity| L2_Decision{Risk Tier}
        
        L2_Decision -->|Critical / Known Dependency| Route_Fix[Target: Auto-Fix]
        L2_Decision -->|Medium / Complex Code| Route_Ticket[Target: Backlog]
        L2_Decision -->|Low / Isolated| Route_Defer[Target: Next Release]
    end

    %% Level 3
    subgraph Level 3: Auto Fix
        Route_Fix --> L3_MR[GitLab: Auto-Generate Merge Request]
        L3_MR --> L3_Bump[Update Library e.g., Spring Boot 2.5.1 to 2.5.15]
        L3_Bump --> L3_Test[Run Pipeline Tests]
        L3_Test --> L3_Deploy[Auto-Deploy Secure Version]
        L3_Deploy -.->|Zero Manual Work| L5_Telemetry
    end

    %% Level 4
    subgraph Level 4: Auto Ownership
        Route_Ticket --> L4_CMDB[Query CMDB: Map App to Owner]
        L4_CMDB --> L4_Ticket[ServiceNow: Auto-Create Incident]
        L4_Ticket --> L4_Assign[Auto-Assign to Operations Support Team]
        L4_Assign -.-> L5_Telemetry
        Route_Defer -.-> L5_Telemetry
    end

    %% Level 5
    subgraph Level 5: Auto Governance
        L5_Telemetry[(Security & Operations Telemetry)]
        L5_Telemetry --> L5_Splunk[Splunk: Vuln Count & Compliance Posture]
        L5_Telemetry --> L5_Dyna[Dynatrace: SLA Breaches & Team Rankings]
    end
    
    %% Styling
    classDef level1 fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef level2 fill:#fff3e0,stroke:#f57c00,stroke-width:2px;
    classDef level3 fill:#e8f5e9,stroke:#388e3c,stroke-width:2px;
    classDef level4 fill:#f3e5f
