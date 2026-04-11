package com.brainify.quizapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.brainify.quizapp.models.Question
import java.util.ArrayList

class QuizDbHelper(context: Context) : SQLiteOpenHelper(context, "BrainifyQuiz.db", null, 1200) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS questions (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, topic TEXT, question TEXT, option1 TEXT, option2 TEXT, option3 TEXT, option4 TEXT, answer INTEGER, level TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT UNIQUE, phone TEXT, password TEXT)")
        insertAllQuestions(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS questions")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun insertAllQuestions(db: SQLiteDatabase) {
        val subjects = mapOf(
            "Networking" to listOf("OSI Model", "TCP/IP Protocol", "IP Addressing", "Network Topology", "Routing & Switching", "Network Security"),
            "Machine Learning" to listOf("Supervised Learning", "Unsupervised Learning", "Neural Networks", "Decision Trees", "NLP Basics", "Reinforcement Learning"),
            "Cloud Computing" to listOf("IaaS, PaaS, SaaS", "Deployment Models", "Virtualization", "AWS Basics", "Azure Basics", "Cloud Security"),
            "Parallel Computing" to listOf("Parallel Architecture", "Shared Memory Systems", "Distributed Computing", "Pthreads & OpenMP", "MPI Programming", "CUDA & GPU Computing"),
            "Web Development" to listOf("HTML5 & CSS3", "JavaScript Essentials", "Responsive Design", "React Basics", "Node.js & Express", "RESTful APIs"),
            "Accounting" to listOf("Accounting Principles", "Journal & Ledger", "Balance Sheets", "Cash Flow Statements", "Financial Auditing", "Cost Accounting")
        )

        for ((subject, topics) in subjects) {
            for (topic in topics) {
                for (i in 0..14) {
                    val nQ = getExpertMCQ(subject, topic, "Normal", i)
                    addQ(db, subject, topic, nQ.q, nQ.o1, nQ.o2, nQ.o3, nQ.o4, nQ.ans, "Normal")
                    
                    val hQ = getExpertMCQ(subject, topic, "Hard", i)
                    addQ(db, subject, topic, hQ.q, hQ.o1, hQ.o2, hQ.o3, hQ.o4, hQ.ans, "Hard")
                }
            }
        }
    }

    private fun getExpertMCQ(s: String, t: String, l: String, i: Int): QData {
        val (qList, opPool) = when (s) {
            "Networking" -> (if (l == "Normal") netN else netH) to poolNet
            "Machine Learning" -> (if (l == "Normal") mlN else mlH) to poolML
            "Cloud Computing" -> (if (l == "Normal") cloudN else cloudH) to poolCloud
            "Parallel Computing" -> (if (l == "Normal") paraN else paraH) to poolPara
            "Web Development" -> (if (l == "Normal") webN else webH) to poolWeb
            "Accounting" -> (if (l == "Normal") accN else accH) to poolAcc
            else -> listOf("Advanced theoretical analysis of \$t in \$s.") to poolNet
        }

        val template = if (i < qList.size) qList[i] else "Critically assess the architectural impact of \$t on \$s distributed throughput (Analysis Q${i+1})"
        val qText = template.replace("\$t", t).replace("\$s", s)

        return QData(qText, opPool[i % opPool.size], opPool[(i+3) % opPool.size], opPool[(i+7) % opPool.size], opPool[(i+11) % opPool.size], (i % 4))
    }

    // --- High Difficulty Technical Questions ---
    private val netN = listOf("Examine the mathematical addressing exhaustion limits of \$t implementation in a global ASN.", "Analyze the impact of propagation delay on \$t flow control stability in 100Gbps links.", "Critically analyze the header overhead ratio in \$t data encapsulation for small payloads.", "Evaluate the convergence time complexity of link-state algorithms within a \$t framework.", "Discuss the role of \$t in determining maximum transmission unit (MTU) fragmentation efficiency.", "Identify the core multiplexing mechanism that prevents spectral collision in \$t streams.", "Examine the role of ARP spoofing prevention within high-latency \$t resolution cycles.", "Evaluate the efficiency of error-correcting codes (ECC) vs parity detection in \$t.", "Analyze the impact of VLAN tagging on \$t throughput in multi-tenant cloud fabrics.", "Discuss the architectural role of Anycast in global \$t distribution and load balancing.", "Analyze how \$t influences the signal-to-noise ratio in high-frequency band environments.", "Evaluate the security implications of \$t transmission within untrusted DMZ domains.", "Describe the logical segmentation layers occurring during a \$t multi-stage handshake.", "Examine the relationship between \$t latency and TCP window scaling factors.", "Critically review the DNSSEC trust chain implementation within \$t core infrastructure.")
    private val netH = listOf("Critically evaluate the BGP path vector protocol impact on \$t convergence under flash-crowd stress.", "Analyze the mathematical complexity of CRC-32 generator polynomials used in \$t error detection.", "Discuss the architectural trade-offs of SDN centralized controllers vs traditional \$t control planes.", "Evaluate the impact of packet reordering on \$t throughput in high-speed long-fat-pipes (LFNs).", "Analyze the security vulnerabilities of dual-stack IPv6 transition mechanisms in \$t architecture.", "Examine the role of Label Distribution Protocol (LDP) in \$t MPLS traffic engineering.", "Analyze the queueing theory models (M/M/1/K) for \$t buffer overflow and packet drop management.", "Critically assess the impact of TCP Slow Start on \$t congestion window dynamics in satellite links.", "Analyze the low-level interrupt handling and NAPI routines of high-performance \$t NICs.", "Evaluate the efficiency of non-deterministic CSMA/CD vs deterministic Token-passing in saturated \$t.", "Discuss the architectural implications of \$t in Zero-Trust software-defined perimeter models.", "Analyze the mathematical entropy of random early detection (RED) algorithms in \$t buffers.", "Examine the role of ICMP type-3 code-4 in Path MTU Discovery for \$t flow control.", "Critically review the impact of TLS 1.3 0-RTT resumption on \$t application-layer security.", "Analyze the impact of \$t on the CAP theorem constraints in distributed BGP-based routing.")

    private val mlN = listOf("Analyze the bias-variance tradeoff impact on \$t model generalization error in high dimensions.", "Evaluate the mathematical convergence properties of SGD vs Adam optimizers in non-convex \$t.", "Explain the role of feature scaling in ensuring \$t gradient descent numerical stability.", "Discuss the convergence rate of the loss function in \$t high-dimensional manifold optimization.", "Examine the use of stratified K-fold cross-validation for \$t hyperparameter fine-tuning.", "Identify the mathematical basis of information gain vs Gini impurity in recursive \$t trees.", "Analyze the role of L1/L2 regularization in preventing \$t overfitting via weight decay.", "Evaluate the performance of KNN algorithms under the curse of dimensionality in sparse \$t.", "Discuss the impact of class imbalance on \$t precision-recall curves vs ROC-AUC metrics.", "Analyze the application of the kernel trick in non-linear \$t mapping to Hilbert space.", "Explain the mathematical difference between bagging and boosting ensemble variance in \$t.", "Examine the role of the learning rate scheduler in deep \$t neural network training convergence.", "Evaluate the effectiveness of \$t in identifying multi-modal latent patterns in noisy data.", "Discuss the role of PCA in \$t dimensionality reduction and cumulative variance retention.", "Analyze the logic of decision boundary optimization in probabilistic \$t generative models.")
    private val mlH = listOf("Critically assess the Hessian matrix impact on the local convergence of \$t second-order optimizers.", "Analyze the vanishing and exploding gradient problems in deep recurrent \$t architectures.", "Evaluate the mathematical derivation of backpropagation in complex DAG-based \$t architectures.", "Discuss the role of Kullback-Leibler divergence in generative \$t variational autoencoders.", "Analyze the architectural impact of multi-head self-attention mechanisms in Transformer \$t.", "Evaluate the mathematical scalability of distributed \$t across heterogeneous multi-GPU clusters.", "Critically review the Bayesian inference models within \$t probabilistic graphical frameworks.", "Analyze the impact of non-stationary data distributions on \$t online learning and concept drift.", "Evaluate the role of Monte Carlo simulations in reinforcement \$t policy gradient methods.", "Analyze the interpretability vs accuracy tradeoff in high-capacity black-box \$t models.", "Critically assess the impact of dropout layers on the generalization gap in deep learning \$t.", "Analyze the mathematical foundations of \$t neural architecture search (NAS) optimization.", "Discuss the role of transfer learning in low-resource domain \$t task-specific optimization.", "Analyze the impact of weight quantization on \$t inference latency and precision loss.", "Evaluate the convergence of Expectation-Maximization (EM) in GMM-based \$t clustering.")

    // --- Option Pools ---
    private val poolNet = listOf("Encapsulation", "BGP Convergence", "Path Vector", "CRC-32 Checksum", "IPsec ESP/AH", "MPLS LDP", "QoS Jitter", "M/M/1 Queue", "Anycast Routing", "TCP Slow Start", "VLAN 802.1Q", "DNSSEC Root", "CSMA/CD Logic", "MTU Fragment", "IPv6 Dual Stack")
    private val poolML = listOf("Gradient Descent", "Bias-Variance", "Backpropagation", "Regularization", "Neural Net", "Hessian Matrix", "Transformer", "Adam Optimizer", "Cross-entropy", "SVM Kernel", "Random Forest", "LSTM Cell", "KL Divergence", "CNN Pooling", "PCA Variance")
    private val poolCloud = listOf("Hypervisor", "Elasticity", "Multi-tenancy", "IaaS Logic", "Provisioning", "Virtualization", "Cloud Native", "Microservices", "Orchestration", "Docker Engine", "S3 Storage", "IAM Policy", "VPC Peering", "API Gateway", "Serverless")
    private val poolPara = listOf("Amdahl's Law", "Deadlock", "Race Condition", "Thread Sync", "Shared Memory", "MPI Protocol", "CUDA Cores", "NUMA Affinity", "Pthread Lock", "Mutex Sem", "GPU Kernel", "Throughput", "Latency", "Cache Coherency", "Barrier")
    private val poolWeb = listOf("Virtual DOM", "Event Loop", "SSR/CSR", "Hydration", "Middleware", "JWT Auth", "RESTful API", "CORS Policy", "Redux State", "Tree-shaking", "WebSocket", "XSS Attack", "Service Worker", "Shadow DOM", "OAuth2 Flow")
    private val poolAcc = listOf("Debit/Credit", "Equity Balance", "Audit Trail", "Tax Deferred", "Revenue Recog", "Asset Growth", "Liability", "Fiscal Year", "Ledger Post", "Journal Entry", "Cash Flow", "Depreciation", "Amortization", "Balance Sheet", "Trial Balance")

    // --- Higher Education Subject Data ---
    private val cloudN = listOf("Analyze the hypervisor virtualization overhead in \$t resource isolation architecture.", "Evaluate the impact of \$t on cloud elasticity and auto-scaling logic performance.", "Discuss the role of \$t in ensuring multi-tenant data privacy and isolation semantics.", "Examine the storage consistency models (PACELC) used in global \$t implementations.", "Identify the primary network isolation techniques in multi-region \$t VPC architectures.", "Analyze the cost optimization algorithms for \$t provisioning in spot-instance markets.", "Evaluate the performance impact of \$t sidecar patterns in distributed microservices.", "Discuss the role of \$t in cloud-native identity and access management (IAM) trust models.", "Examine the regional redundancy strategies for \$t high availability and disaster recovery.", "Analyze the throughput of \$t block storage vs object storage in high-I/O cloud tasks.", "Evaluate the latency introduced by \$t API gateway middleware and rate-limiting filters.", "Discuss the compliance auditing requirements for \$t within regulated cloud environments.", "Examine the role of \$t in cloud-native serverless environments and cold-start latency.", "Analyze the scalability of \$t control planes in massive Kubernetes-based clusters.", "Evaluate the impact of \$t on disaster recovery recovery-point-objectives (RPO).")
    private val cloudH = listOf("Critically assess the hypervisor introspection challenges in \$t security and malware detection.", "Analyze the federated identity interoperability in multi-cloud \$t SSO implementations.", "Evaluate the impact of \$t on edge computing latency and throughput at the network fringe.", "Discuss the architectural trade-offs of \$t in serverless function-as-a-service (FaaS).", "Analyze the orchestration complexity of \$t in hybrid cloud models vs pure-cloud models.", "Critically review the security hardening of \$t container runtimes and kernel isolation.", "Analyze the mathematical models of \$t billing and tiered cost allocation algorithms.", "Evaluate the impact of \$t on software-defined networking (SDN) data-plane performance.", "Discuss the role of \$t in maintaining eventual consistency in globally distributed stores.", "Examine the cryptographic verification processes in \$t cloud trust models and HSMs.", "Analyze the impact of \$t on the CAP theorem constraints in distributed cloud databases.", "Evaluate the efficiency of \$t in large-scale data migration cycles and delta-syncs.", "Discuss the architectural role of \$t in cloud service mesh ecosystems and mTLS.", "Analyze the low-level resource contention in \$t multi-tenant CPU and memory schedulers.", "Critically assess the scalability of \$t metadata management engines in object storage.")
    
    private val paraN = listOf("Analyze the speedup limits of \$t using Amdahl's Law and serial fractions.", "Evaluate the impact of thread synchronization primitives on \$t throughput efficiency.", "Examine the shared memory consistency models (TSO vs PSO) in \$t systems.", "Discuss the role of mutual exclusion in preventing \$t race conditions and data corruption.", "Identify the primary task decomposition strategies for recursive \$t algorithms.", "Analyze the memory hierarchy impact on \$t core utilization and cache line misses.", "Evaluate the efficiency of message-passing interfaces (MPI) in \$t inter-node IPC.", "Discuss the role of POSIX threads (pthreads) in \$t low-level concurrency models.", "Examine the impact of cache coherence protocols (MESI) on multi-socket \$t performance.", "Analyze the job scheduling algorithms for \$t distributed nodes under heterogeneous load.", "Evaluate the scalability of \$t worker thread pools in high-concurrency server tasks.", "Discuss the role of barrier synchronization in iterative \$t data flow processing.", "Examine the impact of signal handling on non-deterministic concurrent \$t execution.", "Analyze the data partitioning strategies for \$t parallel processing in Big Data tasks.", "Evaluate the impact of \$t on multicore processor power-density and heat dissipation.")
    private val paraH = listOf("Critically analyze the race condition detection logic in multi-threaded \$t systems.", "Evaluate the impact of livelock and starvation on \$t core efficiency and job completion.", "Discuss the architectural trade-offs of \$t in GPU kernel optimization and occupancy.", "Analyze the mathematical limit of parallel \$t under Gustafson's Law scaling.", "Evaluate the impact of false sharing on \$t cache line performance in L1 caches.", "Discuss the architectural role of NUMA affinity in high-scale multi-socket \$t.", "Analyze the scalability of distributed \$t hash table implementations and rebalancing.", "Critically assess the sequential consistency models vs release consistency in \$t.", "Analyze the impact of interconnection topologies (Torus vs Fat-tree) on \$t node latency.", "Discuss the role of \$t in optimizing vector processing (SIMD) units for tensor math.", "Evaluate the efficiency of dynamic scheduling in heterogeneous \$t CPU-GPU systems.", "Analyze the mathematical entropy of \$t load balancing algorithms in distributed nodes.", "Critically review the deadlock prevention vs avoidance vs detection algorithms in \$t.", "Analyze the impact of atomic operations on \$t memory bus contention and lock-free lists.", "Discuss the role of \$t in large-scale fault-tolerant parallel computing and checkpointing.")

    private val webN = listOf("Analyze the Virtual DOM reconciliation algorithm efficiency in \$t UI frameworks.", "Evaluate the impact of event delegation on \$t browser memory usage and bubble cycles.", "Discuss the role of closures in \$t asynchronous state management and scope chains.", "Examine the performance trade-offs between SSR, CSR, and Static-site generation in \$t.", "Identify the core protocols for real-time \$t data exchange (WebSockets vs SSE).", "Analyze the impact of middleware on \$t request-response cycles and security headers.", "Evaluate the security implications of JWT authentication vs Session-based auth in \$t.", "Discuss the role of state management libraries (Redux/MobX) in complex \$t apps.", "Examine the impact of tree-shaking on \$t bundle size optimization and dead-code removal.", "Analyze the routing logic in single-page \$t architectures and browser history APIs.", "Evaluate the efficiency of RESTful vs GraphQL APIs in data-intensive \$t applications.", "Discuss the role of CORS policies in cross-origin \$t requests and preflight cycles.", "Examine the impact of HTTP/2 multiplexing on \$t resource loading times and priority.", "Analyze the memory management and garbage collection patterns of Node.js in \$t.", "Evaluate the role of service workers in \$t progressive web apps and offline caching.")
    private val webH = listOf("Critically evaluate the reconciliation algorithm depth and heuristic patterns in \$t UI.", "Analyze the asynchronous non-blocking I/O performance in high-concurrency \$t backends.", "Discuss the architectural impact of hydration and island architecture in \$t frameworks.", "Evaluate the security risks of XSS, CSRF, and Prototype Pollution in modern \$t JS.", "Analyze the memory leak detection patterns in \$t state hooks and component lifecycles.", "Critically review the Content Security Policy (CSP) in \$t defense-in-depth strategies.", "Discuss the architectural role of WebSockets in real-time \$t scale and proxy handling.", "Analyze the impact of code splitting on \$t initial paint latency and TTI metrics.", "Evaluate the efficiency of shadow DOM encapsulation in \$t web component standards.", "Discuss the role of OAuth2 and OpenID Connect flows in \$t identity provider ecosystems.", "Analyze the low-level memory usage of V8 engine in \$t environments and heap limits.", "Critically assess the scalability of \$t backend middleware pipelines and worker threads.", "Evaluate the impact of \$t on SEO, web crawler accessibility, and dynamic rendering.", "Discuss the role of \$t in building micro-frontend architectures and module federation.", "Analyze the impact of browser caching strategies (ETags/Service workers) on \$t performance.")

    private val accN = listOf("Analyze the foundational role of \$t in financial reporting accuracy and transparency.", "Evaluate the impact of \$t on asset valuation methodologies under historical cost vs fair value.", "Discuss the role of equity structure in \$t accounting and shareholder reporting standards.", "Examine the audit trail maintenance requirements for \$t business process cycles.", "Identify the primary tax compliance principles within \$t across international domains.", "Analyze the revenue recognition standards (IFRS 15) for long-term \$t contracts.", "Evaluate the impact of depreciation on \$t fixed asset ledgers and tax shield logic.", "Discuss the logic of cash flow statement preparation using the direct vs indirect \$t method.", "Examine the accuracy of journal entry recording in \$t integrated ERP systems.", "Analyze the balance sheet reconciliation process for \$t accounts and unadjusted trials.", "Evaluate the categorizations of liabilities in complex \$t and long-term debt covenants.", "Discuss the role of internal auditing in ensuring \$t data integrity and fraud prevention.", "Examine the fiscal year closing procedures for global \$t and consolidation of subs.", "Analyze the impact of inventory valuation (FIFO/LIFO) on \$t gross profit margins.", "Evaluate the role of \$t in managerial cost-benefit analysis and break-even points.")
    private val accH = listOf("Critically analyze the impact of deferred tax assets and liabilities on \$t reporting.", "Evaluate the valuation of intangible assets and goodwill in \$t merger and acquisitions.", "Discuss the consolidation of financial statements in multi-parent \$t holding structures.", "Analyze the liquidity and solvency ratios within \$t systems under IFRS standards.", "Evaluate the amortization of complex debt instruments and effective interest in \$t.", "Discuss the revenue recognition challenges in multi-period contract \$t and performance obligations.", "Analyze the provisioning for contingent liabilities in \$t under IAS 37 constraints.", "Critically assess the impairment testing of long-lived \$t assets and cash-generating units.", "Evaluate the ethical implications of creative \$t reporting and earnings management.", "Discuss the IFRS vs GAAP standards in global \$t implementations and convergence issues.", "Analyze the impact of \$t on capital expenditure (CAPEX) planning and ROI forecasting.", "Evaluate the cash flow discounting models in \$t net-present-value (NPV) analysis.", "Discuss the foreign currency translation impacts on global \$t and OCI reporting.", "Analyze the role of forensic accounting in \$t fraud detection and investigative auditing.", "Critically review the \$t impact on shareholder value, dividend policy, and equity growth.")

    private data class QData(val q: String, val o1: String, val o2: String, val o3: String, val o4: String, val ans: Int)

    private fun addQ(db: SQLiteDatabase, s: String, t: String, q: String, o1: String, o2: String, o3: String, o4: String, a: Int, lvl: String) {
        val v = ContentValues().apply {
            put("subject", s); put("topic", t); put("question", q)
            put("option1", o1); put("option2", o2); put("option3", o3); put("option4", o4)
            put("answer", a); put("level", lvl)
        }
        db.insert("questions", null, v)
    }

    // --- Core API ---
    fun getQuestions(subject: String, topic: String, level: String): ArrayList<Question> {
        val list = ArrayList<Question>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM questions WHERE TRIM(LOWER(subject)) = TRIM(LOWER(?)) AND TRIM(LOWER(topic)) = TRIM(LOWER(?)) AND TRIM(LOWER(level)) = TRIM(LOWER(?)) ORDER BY RANDOM()", 
            arrayOf(subject, topic, level)
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(Question(cursor.getString(3), listOf(cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)), cursor.getInt(8), level = cursor.getString(9)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun verifyUser(email: String, pass: String): Map<String, String>? {
        val cursor = readableDatabase.rawQuery("SELECT username, phone, password FROM users WHERE LOWER(email)=LOWER(?) AND password=?", arrayOf(email.trim(), pass))
        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) user = mapOf("username" to cursor.getString(0), "phone" to cursor.getString(1), "password" to cursor.getString(2))
        cursor.close()
        return user
    }

    fun registerUser(u: String, e: String, ph: String, p: String): Boolean {
        val v = ContentValues().apply { put("username", u); put("email", e.trim().lowercase()); put("phone", ph); put("password", p) }
        return try { writableDatabase.insert("users", null, v) != -1L } catch (ex: Exception) { false }
    }

    fun getPasswordByEmail(email: String): String? {
        val cursor = readableDatabase.rawQuery("SELECT password FROM users WHERE LOWER(email)=LOWER(?)", arrayOf(email.trim()))
        var pass: String? = null
        if (cursor.moveToFirst()) pass = cursor.getString(0)
        cursor.close()
        return pass
    }
}
