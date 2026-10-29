-- Curated sample bibliography. Descriptions and reading notes are original editorial text.

-- Demo capacities are illustrative. Existing book IDs, capacities and loan history are preserved.

-- Cover thumbnails use the source URLs as requested by Open Library; offline typography is the fallback.

UPDATE books SET author = 'Martin Kleppmann', description = '从数据模型、存储引擎到复制与分区，这本书把日常开发中零散的技术选择放回完整系统中。它关注的不只是某个数据库如何使用，更是可靠性、一致性与可维护性之间如何取舍。适合在掌握业务开发后，建立系统设计的整体视角。', category = '数据库与系统', audience = '有数据库使用经验，希望理解分布式数据系统的后端工程师。', highlights = '数据模型与存储引擎的取舍
复制、分区与事务一致性
批处理与流处理的设计思路', cover_url = 'https://covers.openlibrary.org/b/id/8434671-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19293745W', featured = TRUE WHERE title = 'Designing Data-Intensive Applications';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Designing Data-Intensive Applications', 'Martin Kleppmann', '从数据模型、存储引擎到复制与分区，这本书把日常开发中零散的技术选择放回完整系统中。它关注的不只是某个数据库如何使用，更是可靠性、一致性与可维护性之间如何取舍。适合在掌握业务开发后，建立系统设计的整体视角。', '数据库与系统', '有数据库使用经验，希望理解分布式数据系统的后端工程师。', '数据模型与存储引擎的取舍
复制、分区与事务一致性
批处理与流处理的设计思路', 'https://covers.openlibrary.org/b/id/8434671-M.jpg?default=false', 'https://openlibrary.org/works/OL19293745W', TRUE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Designing Data-Intensive Applications');

UPDATE books SET author = 'Joshua Bloch', description = '围绕真实代码中的设计选择，讨论对象创建、类与接口、泛型、枚举、异常以及并发等主题。它将语言知识转化为可在代码评审中使用的判断标准，帮助读者理解一段 Java 代码为何容易维护，又可能隐藏哪些约束。', category = '后端与 Java', audience = '已经能够编写 Java 程序，希望提高 API 设计与代码质量的开发者。', highlights = '对象创建与资源生命周期
泛型、接口与方法契约
异常与并发代码的设计原则', cover_url = 'https://covers.openlibrary.org/b/id/1176573-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL6223299W', featured = TRUE WHERE title = 'Effective Java';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Effective Java', 'Joshua Bloch', '围绕真实代码中的设计选择，讨论对象创建、类与接口、泛型、枚举、异常以及并发等主题。它将语言知识转化为可在代码评审中使用的判断标准，帮助读者理解一段 Java 代码为何容易维护，又可能隐藏哪些约束。', '后端与 Java', '已经能够编写 Java 程序，希望提高 API 设计与代码质量的开发者。', '对象创建与资源生命周期
泛型、接口与方法契约
异常与并发代码的设计原则', 'https://covers.openlibrary.org/b/id/1176573-M.jpg?default=false', 'https://openlibrary.org/works/OL6223299W', TRUE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Effective Java');

UPDATE books SET author = 'Eric Evans', description = '把软件建模与业务理解联系起来，讨论通用语言、实体、值对象、聚合和限界上下文。它提醒团队，模型的价值不在类图有多复杂，而在代码与业务沟通是否使用同一套概念，以及边界能否随着业务演进。', category = '架构与工程', audience = '需要梳理复杂业务边界的工程师、架构师与技术负责人。', highlights = '以通用语言连接业务与代码
实体、值对象与聚合边界
限界上下文和模型的持续演进', cover_url = 'https://covers.openlibrary.org/b/id/5548424-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL4464385W', featured = FALSE WHERE title = 'Domain-Driven Design';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Domain-Driven Design', 'Eric Evans', '把软件建模与业务理解联系起来，讨论通用语言、实体、值对象、聚合和限界上下文。它提醒团队，模型的价值不在类图有多复杂，而在代码与业务沟通是否使用同一套概念，以及边界能否随着业务演进。', '架构与工程', '需要梳理复杂业务边界的工程师、架构师与技术负责人。', '以通用语言连接业务与代码
实体、值对象与聚合边界
限界上下文和模型的持续演进', 'https://covers.openlibrary.org/b/id/5548424-M.jpg?default=false', 'https://openlibrary.org/works/OL4464385W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Domain-Driven Design');

UPDATE books SET author = 'Martin Fowler', description = '用一组可识别、可验证的小步骤改善既有代码，在保持外部行为的同时调整内部结构。它通过代码坏味道、重构手法和测试反馈，说明如何让日常修改更安全，也让设计改进成为持续开发的一部分。', category = '架构与工程', audience = '经常维护既有业务代码，想提升修改安全性与可读性的开发者。', highlights = '识别代码坏味道
通过小步骤保持行为稳定
让测试支持持续的设计改进', cover_url = 'https://covers.openlibrary.org/b/id/7087623-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL16551230W', featured = FALSE WHERE title = 'Refactoring';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Refactoring', 'Martin Fowler', '用一组可识别、可验证的小步骤改善既有代码，在保持外部行为的同时调整内部结构。它通过代码坏味道、重构手法和测试反馈，说明如何让日常修改更安全，也让设计改进成为持续开发的一部分。', '架构与工程', '经常维护既有业务代码，想提升修改安全性与可读性的开发者。', '识别代码坏味道
通过小步骤保持行为稳定
让测试支持持续的设计改进', 'https://covers.openlibrary.org/b/id/7087623-M.jpg?default=false', 'https://openlibrary.org/works/OL16551230W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Refactoring');

UPDATE books SET author = 'David Thomas and Andrew Hunt', description = '围绕开发者的日常判断，讨论知识积累、需求沟通、自动化、反馈和对代码的责任。内容跨越具体语言，强调在不确定环境中主动验证假设，把工程习惯与长期成长联系起来。', category = '架构与工程', audience = '希望改进工作方式、沟通与工程判断的各阶段开发者。', highlights = '主动管理知识与反馈
把自动化融入日常开发
在需求与实现之间作出清晰判断', cover_url = 'https://covers.openlibrary.org/b/id/10143650-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL5748544W', featured = FALSE WHERE title = 'The Pragmatic Programmer';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'The Pragmatic Programmer', 'David Thomas and Andrew Hunt', '围绕开发者的日常判断，讨论知识积累、需求沟通、自动化、反馈和对代码的责任。内容跨越具体语言，强调在不确定环境中主动验证假设，把工程习惯与长期成长联系起来。', '架构与工程', '希望改进工作方式、沟通与工程判断的各阶段开发者。', '主动管理知识与反馈
把自动化融入日常开发
在需求与实现之间作出清晰判断', 'https://covers.openlibrary.org/b/id/10143650-M.jpg?default=false', 'https://openlibrary.org/works/OL5748544W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'The Pragmatic Programmer');

UPDATE books SET author = 'Robert C. Martin', description = '从命名、函数、对象、错误处理和测试出发，讨论代码如何准确表达意图。读者可以把其中的规则与自己的业务上下文对照，在评审中区分机械套用规范与真正降低理解成本的改进。', category = '架构与工程', audience = '希望让业务代码更易读、更易评审的开发者。', highlights = '命名与函数的表达力
错误处理和边界设计
通过测试与重构改善可维护性', cover_url = 'https://covers.openlibrary.org/b/id/8065615-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL17618370W', featured = FALSE WHERE title = 'Clean Code';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Clean Code', 'Robert C. Martin', '从命名、函数、对象、错误处理和测试出发，讨论代码如何准确表达意图。读者可以把其中的规则与自己的业务上下文对照，在评审中区分机械套用规范与真正降低理解成本的改进。', '架构与工程', '希望让业务代码更易读、更易评审的开发者。', '命名与函数的表达力
错误处理和边界设计
通过测试与重构改善可维护性', 'https://covers.openlibrary.org/b/id/8065615-M.jpg?default=false', 'https://openlibrary.org/works/OL17618370W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Clean Code');

UPDATE books SET author = 'Robert C. Martin', description = '讨论系统中的职责、组件与依赖方向，关注业务规则如何与数据库、界面和框架保持合适的边界。它提供的是分析变化成本的视角，读者需要结合系统体量判断抽象是否值得。', category = '架构与工程', audience = '开始承担模块设计与架构演进工作的工程师。', highlights = '组件职责和依赖方向
业务规则与外部机制的边界
从变化成本评估架构选择', cover_url = 'https://covers.openlibrary.org/b/id/8605114-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19809141W', featured = FALSE WHERE title = 'Clean Architecture';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Clean Architecture', 'Robert C. Martin', '讨论系统中的职责、组件与依赖方向，关注业务规则如何与数据库、界面和框架保持合适的边界。它提供的是分析变化成本的视角，读者需要结合系统体量判断抽象是否值得。', '架构与工程', '开始承担模块设计与架构演进工作的工程师。', '组件职责和依赖方向
业务规则与外部机制的边界
从变化成本评估架构选择', 'https://covers.openlibrary.org/b/id/8605114-M.jpg?default=false', 'https://openlibrary.org/works/OL19809141W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Clean Architecture');

UPDATE books SET author = 'Erich Gamma, Richard Helm, Ralph Johnson and John Vlissides', description = '以可复用的面向对象协作方式整理常见设计问题，覆盖创建、结构与行为模式。理解模式的动机与适用条件，比记住类图更重要：它们帮助团队讨论方案，也需要警惕为了模式而增加复杂度。', category = '架构与工程', audience = '熟悉面向对象基础，希望建立设计讨论词汇的开发者。', highlights = '创建型、结构型与行为型协作
模式的动机、代价与适用条件
在具体上下文中选择抽象', cover_url = 'https://covers.openlibrary.org/b/id/6601119-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL6030812W', featured = FALSE WHERE title = 'Design Patterns';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson and John Vlissides', '以可复用的面向对象协作方式整理常见设计问题，覆盖创建、结构与行为模式。理解模式的动机与适用条件，比记住类图更重要：它们帮助团队讨论方案，也需要警惕为了模式而增加复杂度。', '架构与工程', '熟悉面向对象基础，希望建立设计讨论词汇的开发者。', '创建型、结构型与行为型协作
模式的动机、代价与适用条件
在具体上下文中选择抽象', 'https://covers.openlibrary.org/b/id/6601119-M.jpg?default=false', 'https://openlibrary.org/works/OL6030812W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Design Patterns');

UPDATE books SET author = 'Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes and Doug Lea', description = '从线程安全、共享状态和对象发布讲起，逐步进入任务执行、取消、线程池与并发构建块。它将并发正确性与性能分开讨论，帮助读者在看到运行结果之前，先分析代码是否具有可靠的同步边界。', category = '后端与 Java', audience = '需要编写、评审或排查多线程 Java 代码的开发者。', highlights = '线程安全与安全发布
执行器、任务取消和线程池
正确性、活跃性与性能的区分', cover_url = 'https://covers.openlibrary.org/b/id/7069893-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL16512736W', featured = TRUE WHERE title = 'Java Concurrency in Practice';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Java Concurrency in Practice', 'Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes and Doug Lea', '从线程安全、共享状态和对象发布讲起，逐步进入任务执行、取消、线程池与并发构建块。它将并发正确性与性能分开讨论，帮助读者在看到运行结果之前，先分析代码是否具有可靠的同步边界。', '后端与 Java', '需要编写、评审或排查多线程 Java 代码的开发者。', '线程安全与安全发布
执行器、任务取消和线程池
正确性、活跃性与性能的区分', 'https://covers.openlibrary.org/b/id/7069893-M.jpg?default=false', 'https://openlibrary.org/works/OL16512736W', TRUE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Java Concurrency in Practice');

UPDATE books SET author = 'Scott Oaks', description = '围绕 Java 应用的性能分析方法，讨论 JVM、垃圾回收、内存、线程与测量工具。书中的重要线索是先建立可靠的测量，再根据证据定位瓶颈，而不是从某个参数或经验结论直接推断优化效果。', category = '后端与 Java', audience = '希望系统学习 Java 性能测量与 JVM 调优的工程师。', highlights = '建立可比较的性能测量
理解垃圾回收与内存行为
从分析证据定位应用瓶颈', cover_url = 'https://covers.openlibrary.org/b/id/8514235-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19547763W', featured = FALSE WHERE title = 'Java Performance';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Java Performance', 'Scott Oaks', '围绕 Java 应用的性能分析方法，讨论 JVM、垃圾回收、内存、线程与测量工具。书中的重要线索是先建立可靠的测量，再根据证据定位瓶颈，而不是从某个参数或经验结论直接推断优化效果。', '后端与 Java', '希望系统学习 Java 性能测量与 JVM 调优的工程师。', '建立可比较的性能测量
理解垃圾回收与内存行为
从分析证据定位应用瓶颈', 'https://covers.openlibrary.org/b/id/8514235-M.jpg?default=false', 'https://openlibrary.org/works/OL19547763W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Java Performance');

UPDATE books SET author = 'Craig Walls', description = '以应用开发为主线，把依赖注入、Web 接口、数据访问、安全与集成能力串成完整工作流。适合在学习 Spring 的同时建立组件如何协作的认识，并回到真实项目检验配置与边界。', category = '后端与 Java', audience = '掌握 Java 基础，准备构建 Spring 应用的开发者。', highlights = '依赖注入与应用配置
Web 接口和数据访问
安全与系统集成的实践路径', cover_url = 'https://covers.openlibrary.org/b/id/8722096-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL8955978W', featured = FALSE WHERE title = 'Spring in Action';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Spring in Action', 'Craig Walls', '以应用开发为主线，把依赖注入、Web 接口、数据访问、安全与集成能力串成完整工作流。适合在学习 Spring 的同时建立组件如何协作的认识，并回到真实项目检验配置与边界。', '后端与 Java', '掌握 Java 基础，准备构建 Spring 应用的开发者。', '依赖注入与应用配置
Web 接口和数据访问
安全与系统集成的实践路径', 'https://covers.openlibrary.org/b/id/8722096-M.jpg?default=false', 'https://openlibrary.org/works/OL8955978W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Spring in Action');

UPDATE books SET author = 'Laurentiu Spilca', description = '通过应用场景理解认证、授权与安全过滤链，逐步建立请求如何被验证和保护的认识。它适合与实际代码配合阅读，特别是区分登录成功、资源权限和客户端凭据处理这些不同层次的问题。', category = '后端与 Java', audience = '需要设计或维护 Spring 身份与权限功能的后端工程师。', highlights = '认证、授权与过滤链
密码存储与请求保护
在应用边界中理解安全配置', cover_url = 'https://covers.openlibrary.org/b/id/13276048-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL22166282W', featured = FALSE WHERE title = 'Spring Security in Action';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Spring Security in Action', 'Laurentiu Spilca', '通过应用场景理解认证、授权与安全过滤链，逐步建立请求如何被验证和保护的认识。它适合与实际代码配合阅读，特别是区分登录成功、资源权限和客户端凭据处理这些不同层次的问题。', '后端与 Java', '需要设计或维护 Spring 身份与权限功能的后端工程师。', '认证、授权与过滤链
密码存储与请求保护
在应用边界中理解安全配置', 'https://covers.openlibrary.org/b/id/13276048-M.jpg?default=false', 'https://openlibrary.org/works/OL22166282W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Spring Security in Action');

UPDATE books SET author = 'Michael T. Nygard', description = '把关注点从功能完成扩展到系统运行，讨论故障如何传播、容量如何耗尽，以及稳定性模式怎样限制影响范围。它帮助工程师在设计阶段就思考超时、隔离、恢复和运行可见性。', category = '架构与工程', audience = '开始关注系统上线、稳定性与故障处理的工程师。', highlights = '识别常见稳定性风险
用超时与隔离限制故障传播
把运行视角带回设计过程', cover_url = 'https://covers.openlibrary.org/b/id/743624-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL8848832W', featured = FALSE WHERE title = 'Release It!';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Release It!', 'Michael T. Nygard', '把关注点从功能完成扩展到系统运行，讨论故障如何传播、容量如何耗尽，以及稳定性模式怎样限制影响范围。它帮助工程师在设计阶段就思考超时、隔离、恢复和运行可见性。', '架构与工程', '开始关注系统上线、稳定性与故障处理的工程师。', '识别常见稳定性风险
用超时与隔离限制故障传播
把运行视角带回设计过程', 'https://covers.openlibrary.org/b/id/743624-M.jpg?default=false', 'https://openlibrary.org/works/OL8848832W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Release It!');

UPDATE books SET author = 'Sam Newman', description = '从服务边界出发，讨论独立部署、集成、数据一致性、测试与组织协作。它不仅介绍拆分方式，也帮助读者理解微服务带来的运行成本，判断何时保持模块化单体更合适。', category = '架构与工程', audience = '需要评估服务拆分、跨服务协作与演进策略的团队。', highlights = '服务边界与独立部署
跨服务通信和数据一致性
技术选择与团队协作的关系', cover_url = 'https://covers.openlibrary.org/b/id/8513315-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19547194W', featured = TRUE WHERE title = 'Building Microservices';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Building Microservices', 'Sam Newman', '从服务边界出发，讨论独立部署、集成、数据一致性、测试与组织协作。它不仅介绍拆分方式，也帮助读者理解微服务带来的运行成本，判断何时保持模块化单体更合适。', '架构与工程', '需要评估服务拆分、跨服务协作与演进策略的团队。', '服务边界与独立部署
跨服务通信和数据一致性
技术选择与团队协作的关系', 'https://covers.openlibrary.org/b/id/8513315-M.jpg?default=false', 'https://openlibrary.org/works/OL19547194W', TRUE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Building Microservices');

UPDATE books SET author = 'Sam Newman', description = '围绕已有单体如何逐步演进，讨论拆分顺序、数据迁移、协作方式以及过程中的风险控制。书中关注连续交付和渐进变化，适合与当前系统的真实约束一起分析。', category = '架构与工程', audience = '准备从既有系统中提取服务，而非从零设计架构的工程师。', highlights = '选择渐进拆分的切入点
处理数据库与服务边界迁移
在持续交付中控制演进风险', cover_url = 'https://covers.openlibrary.org/b/id/12895074-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL21355484W', featured = FALSE WHERE title = 'Monolith to Microservices';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Monolith to Microservices', 'Sam Newman', '围绕已有单体如何逐步演进，讨论拆分顺序、数据迁移、协作方式以及过程中的风险控制。书中关注连续交付和渐进变化，适合与当前系统的真实约束一起分析。', '架构与工程', '准备从既有系统中提取服务，而非从零设计架构的工程师。', '选择渐进拆分的切入点
处理数据库与服务边界迁移
在持续交付中控制演进风险', 'https://covers.openlibrary.org/b/id/12895074-M.jpg?default=false', 'https://openlibrary.org/works/OL21355484W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Monolith to Microservices');

UPDATE books SET author = 'Mark Richards and Neal Ford', description = '把架构特征、风格、组件与决策记录联系起来，强调架构选择本质上是在约束下进行取舍。内容适合用来组织讨论：先澄清目标和质量属性，再比较候选方案的成本。', category = '架构与工程', audience = '希望从模块设计走向系统架构判断的开发者。', highlights = '识别和权衡架构特征
比较常见架构风格
记录决策与沟通取舍', cover_url = 'https://covers.openlibrary.org/b/id/10193363-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL20872782W', featured = FALSE WHERE title = 'Fundamentals of Software Architecture';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Fundamentals of Software Architecture', 'Mark Richards and Neal Ford', '把架构特征、风格、组件与决策记录联系起来，强调架构选择本质上是在约束下进行取舍。内容适合用来组织讨论：先澄清目标和质量属性，再比较候选方案的成本。', '架构与工程', '希望从模块设计走向系统架构判断的开发者。', '识别和权衡架构特征
比较常见架构风格
记录决策与沟通取舍', 'https://covers.openlibrary.org/b/id/10193363-M.jpg?default=false', 'https://openlibrary.org/works/OL20872782W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Fundamentals of Software Architecture');

UPDATE books SET author = 'Neal Ford, Mark Richards, Pramod Sadalage and Zhamak Dehghani', description = '聚焦分布式架构中难以独立优化的问题，例如服务粒度、数据所有权、事务和工作流。它鼓励读者把不同维度的影响摆在一起，说明某个方案为什么适用于当前场景。', category = '架构与工程', audience = '面对服务与数据边界复杂取舍的架构师和资深工程师。', highlights = '服务粒度和耦合关系
分布式数据与事务取舍
用多维分析表达架构理由', cover_url = 'https://covers.openlibrary.org/b/id/12318698-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL25326737W', featured = FALSE WHERE title = 'Software Architecture: The Hard Parts';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Software Architecture: The Hard Parts', 'Neal Ford, Mark Richards, Pramod Sadalage and Zhamak Dehghani', '聚焦分布式架构中难以独立优化的问题，例如服务粒度、数据所有权、事务和工作流。它鼓励读者把不同维度的影响摆在一起，说明某个方案为什么适用于当前场景。', '架构与工程', '面对服务与数据边界复杂取舍的架构师和资深工程师。', '服务粒度和耦合关系
分布式数据与事务取舍
用多维分析表达架构理由', 'https://covers.openlibrary.org/b/id/12318698-M.jpg?default=false', 'https://openlibrary.org/works/OL25326737W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Software Architecture: The Hard Parts');

UPDATE books SET author = 'Alex Petrov', description = '从存储引擎进入数据库内部，讨论索引结构、日志、存储布局和分布式协作。它帮助读者把外部观察到的查询与持久化行为，连接到底层结构和实现机制。', category = '数据库与系统', audience = '希望深入理解数据库实现与存储原理的工程师。', highlights = '存储引擎与索引结构
日志、持久化与数据布局
分布式数据库的协作机制', cover_url = 'https://covers.openlibrary.org/b/id/14572292-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL21360140W', featured = FALSE WHERE title = 'Database Internals';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Database Internals', 'Alex Petrov', '从存储引擎进入数据库内部，讨论索引结构、日志、存储布局和分布式协作。它帮助读者把外部观察到的查询与持久化行为，连接到底层结构和实现机制。', '数据库与系统', '希望深入理解数据库实现与存储原理的工程师。', '存储引擎与索引结构
日志、持久化与数据布局
分布式数据库的协作机制', 'https://covers.openlibrary.org/b/id/14572292-M.jpg?default=false', 'https://openlibrary.org/works/OL21360140W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Database Internals');

UPDATE books SET author = 'Baron Schwartz, Peter Zaitsev and Vadim Tkachenko', description = '围绕 MySQL 的性能、索引、查询与运行维护展开，强调从负载与测量结果理解数据库行为。适合在真实 SQL、执行计划和业务访问模式之间来回对照。', category = '数据库与系统', audience = '需要分析 SQL、索引与数据库运行问题的后端开发者。', highlights = '分析查询与访问模式
设计索引并理解执行计划
建立数据库性能与运行视角', cover_url = 'https://covers.openlibrary.org/b/id/5076309-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL15705149W', featured = FALSE WHERE title = 'High Performance MySQL';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'High Performance MySQL', 'Baron Schwartz, Peter Zaitsev and Vadim Tkachenko', '围绕 MySQL 的性能、索引、查询与运行维护展开，强调从负载与测量结果理解数据库行为。适合在真实 SQL、执行计划和业务访问模式之间来回对照。', '数据库与系统', '需要分析 SQL、索引与数据库运行问题的后端开发者。', '分析查询与访问模式
设计索引并理解执行计划
建立数据库性能与运行视角', 'https://covers.openlibrary.org/b/id/5076309-M.jpg?default=false', 'https://openlibrary.org/works/OL15705149W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'High Performance MySQL');

UPDATE books SET author = 'Josiah L. Carlson', description = '以应用问题讲解 Redis 的数据结构与使用方式，展示缓存、计数、排序和任务处理等建模思路。阅读时应同时考虑数据生命周期、并发边界以及缓存与主数据库之间的一致性。', category = '数据库与系统', audience = '已经接触 Redis，希望理解数据结构与业务建模关系的开发者。', highlights = '使用数据结构表达业务问题
缓存、计数与排序应用
数据生命周期与一致性边界', cover_url = 'https://covers.openlibrary.org/b/id/8514972-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19548259W', featured = FALSE WHERE title = 'Redis in Action';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Redis in Action', 'Josiah L. Carlson', '以应用问题讲解 Redis 的数据结构与使用方式，展示缓存、计数、排序和任务处理等建模思路。阅读时应同时考虑数据生命周期、并发边界以及缓存与主数据库之间的一致性。', '数据库与系统', '已经接触 Redis，希望理解数据结构与业务建模关系的开发者。', '使用数据结构表达业务问题
缓存、计数与排序应用
数据生命周期与一致性边界', 'https://covers.openlibrary.org/b/id/8514972-M.jpg?default=false', 'https://openlibrary.org/works/OL19548259W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Redis in Action');

UPDATE books SET author = 'Chip Huyen', description = '把模型开发放回数据、部署、监控与持续迭代的完整系统中，关注机器学习应用在真实环境中的变化。适合希望理解模型之外工程工作的读者。', category = 'AI 与机器学习', audience = '连接模型开发与应用落地的工程师和技术负责人。', highlights = '数据与模型的系统关系
部署、监控和反馈循环
在变化的环境中维护模型应用', cover_url = 'https://covers.openlibrary.org/b/id/13314480-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL26768541W', featured = FALSE WHERE title = 'Designing Machine Learning Systems';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Designing Machine Learning Systems', 'Chip Huyen', '把模型开发放回数据、部署、监控与持续迭代的完整系统中，关注机器学习应用在真实环境中的变化。适合希望理解模型之外工程工作的读者。', 'AI 与机器学习', '连接模型开发与应用落地的工程师和技术负责人。', '数据与模型的系统关系
部署、监控和反馈循环
在变化的环境中维护模型应用', 'https://covers.openlibrary.org/b/id/13314480-M.jpg?default=false', 'https://openlibrary.org/works/OL26768541W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Designing Machine Learning Systems');

UPDATE books SET author = 'Chip Huyen', description = '围绕基础模型应用的构建过程，讨论评估、提示、检索、工具使用以及应用运行中的成本与延迟。它把模型调用视为系统的一部分，强调通过可验证的反馈改进应用。', category = 'AI 与机器学习', audience = '准备构建或改进大模型应用的后端与 AI 应用工程师。', highlights = '以评估驱动应用改进
检索、提示与工具集成
权衡成本、延迟与输出质量', cover_url = 'https://covers.openlibrary.org/b/id/15091156-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL39671094W', featured = TRUE WHERE title = 'AI Engineering';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'AI Engineering', 'Chip Huyen', '围绕基础模型应用的构建过程，讨论评估、提示、检索、工具使用以及应用运行中的成本与延迟。它把模型调用视为系统的一部分，强调通过可验证的反馈改进应用。', 'AI 与机器学习', '准备构建或改进大模型应用的后端与 AI 应用工程师。', '以评估驱动应用改进
检索、提示与工具集成
权衡成本、延迟与输出质量', 'https://covers.openlibrary.org/b/id/15091156-M.jpg?default=false', 'https://openlibrary.org/works/OL39671094W', TRUE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'AI Engineering');

UPDATE books SET author = 'Aurélien Géron', description = '通过可运行的实践连接机器学习概念、数据准备、模型训练与评估。内容从常见学习算法逐步进入神经网络，适合边做实验边建立基础。', category = 'AI 与机器学习', audience = '具备 Python 基础，希望通过实践学习机器学习的读者。', highlights = '数据准备与模型评估
常见机器学习算法
神经网络的实践路径', cover_url = 'https://covers.openlibrary.org/b/id/9388208-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL20709638W', featured = FALSE WHERE title = 'Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow', 'Aurélien Géron', '通过可运行的实践连接机器学习概念、数据准备、模型训练与评估。内容从常见学习算法逐步进入神经网络，适合边做实验边建立基础。', 'AI 与机器学习', '具备 Python 基础，希望通过实践学习机器学习的读者。', '数据准备与模型评估
常见机器学习算法
神经网络的实践路径', 'https://covers.openlibrary.org/b/id/9388208-M.jpg?default=false', 'https://openlibrary.org/works/OL20709638W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow');

UPDATE books SET author = 'Ian Goodfellow, Yoshua Bengio and Aaron Courville', description = '从数学与机器学习基础进入深度网络，讨论优化、正则化、卷积、序列建模等核心概念。它适合作为系统学习和反复查阅的理论参考。', category = 'AI 与机器学习', audience = '希望建立深度学习理论框架，并愿意补齐数学基础的读者。', highlights = '数学与机器学习基础
深度网络、优化与正则化
卷积与序列建模的核心思想', cover_url = 'https://covers.openlibrary.org/b/id/8086288-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL17801809W', featured = FALSE WHERE title = 'Deep Learning';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Deep Learning', 'Ian Goodfellow, Yoshua Bengio and Aaron Courville', '从数学与机器学习基础进入深度网络，讨论优化、正则化、卷积、序列建模等核心概念。它适合作为系统学习和反复查阅的理论参考。', 'AI 与机器学习', '希望建立深度学习理论框架，并愿意补齐数学基础的读者。', '数学与机器学习基础
深度网络、优化与正则化
卷积与序列建模的核心思想', 'https://covers.openlibrary.org/b/id/8086288-M.jpg?default=false', 'https://openlibrary.org/works/OL17801809W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Deep Learning');

UPDATE books SET author = 'Lewis Tunstall, Leandro von Werra and Thomas Wolf', description = '围绕 Transformer 在自然语言任务中的应用，连接预训练模型、数据处理、微调和部署。通过具体任务理解模型工具链，适合把抽象概念转化为实验与应用。', category = 'AI 与机器学习', audience = '希望实践自然语言模型与 Transformer 工具链的开发者。', highlights = '预训练模型与任务适配
文本数据处理和模型微调
从实验走向应用部署', cover_url = 'https://covers.openlibrary.org/b/id/14559602-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL25810304W', featured = FALSE WHERE title = 'Natural Language Processing with Transformers';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Natural Language Processing with Transformers', 'Lewis Tunstall, Leandro von Werra and Thomas Wolf', '围绕 Transformer 在自然语言任务中的应用，连接预训练模型、数据处理、微调和部署。通过具体任务理解模型工具链，适合把抽象概念转化为实验与应用。', 'AI 与机器学习', '希望实践自然语言模型与 Transformer 工具链的开发者。', '预训练模型与任务适配
文本数据处理和模型微调
从实验走向应用部署', 'https://covers.openlibrary.org/b/id/14559602-M.jpg?default=false', 'https://openlibrary.org/works/OL25810304W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Natural Language Processing with Transformers');

UPDATE books SET author = 'Aditya Y. Bhargava', description = '以直观例子解释常见算法和数据结构，把复杂度、查找、排序、图和动态规划等概念联系起来。适合在写代码之前先建立清晰的解题模型。', category = '编程与算法', audience = '希望重新建立算法直觉的初学者和应用开发者。', highlights = '理解算法复杂度
查找、排序与图算法
用分解问题建立解题思路', cover_url = 'https://covers.openlibrary.org/b/id/8512926-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19546865W', featured = FALSE WHERE title = 'Grokking Algorithms';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Grokking Algorithms', 'Aditya Y. Bhargava', '以直观例子解释常见算法和数据结构，把复杂度、查找、排序、图和动态规划等概念联系起来。适合在写代码之前先建立清晰的解题模型。', '编程与算法', '希望重新建立算法直觉的初学者和应用开发者。', '理解算法复杂度
查找、排序与图算法
用分解问题建立解题思路', 'https://covers.openlibrary.org/b/id/8512926-M.jpg?default=false', 'https://openlibrary.org/works/OL19546865W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Grokking Algorithms');

UPDATE books SET author = 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein', description = '以严谨的分析方式组织算法设计与数据结构，覆盖基础算法、图、动态规划和复杂性相关主题。适合在已有编程经验后系统补足分析与证明能力。', category = '编程与算法', audience = '希望系统学习算法设计与正确性分析的读者。', highlights = '算法设计和复杂度分析
基础数据结构与图算法
动态规划与正确性推理', cover_url = 'https://covers.openlibrary.org/b/id/2341462-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL4781294W', featured = FALSE WHERE title = 'Introduction to Algorithms';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Introduction to Algorithms', 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein', '以严谨的分析方式组织算法设计与数据结构，覆盖基础算法、图、动态规划和复杂性相关主题。适合在已有编程经验后系统补足分析与证明能力。', '编程与算法', '希望系统学习算法设计与正确性分析的读者。', '算法设计和复杂度分析
基础数据结构与图算法
动态规划与正确性推理', 'https://covers.openlibrary.org/b/id/2341462-M.jpg?default=false', 'https://openlibrary.org/works/OL4781294W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Introduction to Algorithms');

UPDATE books SET author = 'James F. Kurose and Keith W. Ross', description = '从应用层向下理解网络，逐步连接 Web、传输、路由与链路等主题。它帮助应用开发者把请求超时、连接、吞吐等现象放回网络分层中分析。', category = '网络与基础', audience = '希望建立网络系统整体认识的计算机学习者与开发者。', highlights = '从应用理解网络分层
传输与路由的核心机制
把网络现象与系统行为联系起来', cover_url = NULL, source_url = 'https://openlibrary.org/works/OL29353889W', featured = FALSE WHERE title = 'Computer Networking: A Top-Down Approach';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Computer Networking: A Top-Down Approach', 'James F. Kurose and Keith W. Ross', '从应用层向下理解网络，逐步连接 Web、传输、路由与链路等主题。它帮助应用开发者把请求超时、连接、吞吐等现象放回网络分层中分析。', '网络与基础', '希望建立网络系统整体认识的计算机学习者与开发者。', '从应用理解网络分层
传输与路由的核心机制
把网络现象与系统行为联系起来', NULL, 'https://openlibrary.org/works/OL29353889W', FALSE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Computer Networking: A Top-Down Approach');

UPDATE books SET author = 'Remzi H. Arpaci-Dusseau and Andrea C. Arpaci-Dusseau', description = '以虚拟化、并发和持久化三条主线组织操作系统知识，解释进程、内存、同步和存储如何支撑应用。适合把日常开发中的线程与文件操作连接到底层机制。', category = '网络与基础', audience = '希望理解应用背后操作系统机制的学生与工程师。', highlights = '进程与内存虚拟化
并发与同步的基本机制
文件系统和持久化原理', cover_url = NULL, source_url = 'https://pages.cs.wisc.edu/~remzi/OSTEP/', featured = FALSE WHERE title = 'Operating Systems: Three Easy Pieces';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Operating Systems: Three Easy Pieces', 'Remzi H. Arpaci-Dusseau and Andrea C. Arpaci-Dusseau', '以虚拟化、并发和持久化三条主线组织操作系统知识，解释进程、内存、同步和存储如何支撑应用。适合把日常开发中的线程与文件操作连接到底层机制。', '网络与基础', '希望理解应用背后操作系统机制的学生与工程师。', '进程与内存虚拟化
并发与同步的基本机制
文件系统和持久化原理', NULL, 'https://pages.cs.wisc.edu/~remzi/OSTEP/', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Operating Systems: Three Easy Pieces');

UPDATE books SET author = 'Randal E. Bryant and David R. O''Hallaron', description = '从程序员视角连接代码、机器执行、内存层次、链接、进程与网络。它关注程序如何真正运行，适合用来建立跨越语言与系统边界的基础。', category = '网络与基础', audience = '希望从应用代码深入理解计算机系统的开发者。', highlights = '程序表示与机器执行
内存层次、链接和进程
从系统角度分析程序行为', cover_url = 'https://covers.openlibrary.org/b/id/8511531-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19545341W', featured = FALSE WHERE title = 'Computer Systems: A Programmer''s Perspective';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Computer Systems: A Programmer''s Perspective', 'Randal E. Bryant and David R. O''Hallaron', '从程序员视角连接代码、机器执行、内存层次、链接、进程与网络。它关注程序如何真正运行，适合用来建立跨越语言与系统边界的基础。', '网络与基础', '希望从应用代码深入理解计算机系统的开发者。', '程序表示与机器执行
内存层次、链接和进程
从系统角度分析程序行为', 'https://covers.openlibrary.org/b/id/8511531-M.jpg?default=false', 'https://openlibrary.org/works/OL19545341W', FALSE, 1 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Computer Systems: A Programmer''s Perspective');

UPDATE books SET author = 'Luciano Ramalho', description = '深入讨论 Python 的数据模型、协议、函数、类型与并发工具，帮助读者从语法使用走向语言惯用设计。适合已有经验后，重新理解对象之间如何协作。', category = '编程与算法', audience = '已经使用 Python，希望提升表达力与设计质量的开发者。', highlights = 'Python 数据模型与协议
函数、类型和对象设计
以语言惯用方式组织程序', cover_url = 'https://covers.openlibrary.org/b/id/8743408-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL19932156W', featured = TRUE WHERE title = 'Fluent Python';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Fluent Python', 'Luciano Ramalho', '深入讨论 Python 的数据模型、协议、函数、类型与并发工具，帮助读者从语法使用走向语言惯用设计。适合已有经验后，重新理解对象之间如何协作。', '编程与算法', '已经使用 Python，希望提升表达力与设计质量的开发者。', 'Python 数据模型与协议
函数、类型和对象设计
以语言惯用方式组织程序', 'https://covers.openlibrary.org/b/id/8743408-M.jpg?default=false', 'https://openlibrary.org/works/OL19932156W', TRUE, 2 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Fluent Python');

UPDATE books SET author = 'Eric Matthes', description = '用基础语法和项目练习建立 Python 开发的起点，把变量、控制流、函数、类与测试串成可执行的学习过程。适合从小作品中获得反馈，再逐步扩展工具能力。', category = '编程与算法', audience = '准备通过动手项目开始学习 Python 的读者。', highlights = '建立扎实的 Python 基础
通过项目连接知识点
用测试和反馈改进程序', cover_url = 'https://covers.openlibrary.org/b/id/8245395-M.jpg?default=false', source_url = 'https://openlibrary.org/works/OL17920482W', featured = FALSE WHERE title = 'Python Crash Course';

INSERT INTO books (title, author, description, category, audience, highlights, cover_url, source_url, featured, total_licenses) SELECT 'Python Crash Course', 'Eric Matthes', '用基础语法和项目练习建立 Python 开发的起点，把变量、控制流、函数、类与测试串成可执行的学习过程。适合从小作品中获得反馈，再逐步扩展工具能力。', '编程与算法', '准备通过动手项目开始学习 Python 的读者。', '建立扎实的 Python 基础
通过项目连接知识点
用测试和反馈改进程序', 'https://covers.openlibrary.org/b/id/8245395-M.jpg?default=false', 'https://openlibrary.org/works/OL17920482W', FALSE, 3 WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Python Crash Course');
