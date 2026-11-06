package com.example.util

/**
 * Assamese (অসমীয়া) translation utility for English sentence learning challenges.
 * Provides natural Assamese translations, phonetic transliterations,
 * and word-by-word bilingual vocabulary bridge breakdowns.
 */
object AssameseTranslator {

    data class TranslationResult(
        val assameseText: String,
        val transliteration: String,
        val wordBreakdown: List<Pair<String, String>> = emptyList()
    )

    // Curated high-accuracy natural translations for academy curriculum
    private val curatedSentences = mapOf(
        "she speaks english with natural confidence and clarity" to TranslationResult(
            assameseText = "তাই স্বাভাৱিক আত্মবিশ্বাস আৰু স্পষ্টতাৰে ইংৰাজী কয়।",
            transliteration = "Tai swabhabik aatmabishwakh aaru spastatare ingraji koy.",
            wordBreakdown = listOf(
                "She" to "তাই (Tai)",
                "speaks" to "কয় (Koy)",
                "English" to "ইংৰাজী (Ingraji)",
                "with" to "সৈতে / -ৰে (Soite / -re)",
                "natural" to "স্বাভাৱিক (Swabhabik)",
                "confidence" to "আত্মবিশ্বাস (Aatmabishwakh)",
                "and" to "আৰু (Aaru)",
                "clarity" to "স্পষ্টতা (Spastata)"
            )
        ),
        "she speaks english with confidence" to TranslationResult(
            assameseText = "তাই আত্মবিশ্বাসেৰে ইংৰাজী কয়।",
            transliteration = "Tai aatmabishwaxeire ingraji koy.",
            wordBreakdown = listOf(
                "She" to "তাই (Tai)",
                "speaks" to "কয় (Koy)",
                "English" to "ইংৰাজী (Ingraji)",
                "with" to "-ৰে (Soite / -re)",
                "confidence" to "আত্মবিশ্বাস (Aatmabishwakh)"
            )
        ),
        "clear pronunciation helps listeners understand your message easily" to TranslationResult(
            assameseText = "স্পষ্ট উচ্চাৰণে শ্ৰোতাসকলক আপোনাৰ বাৰ্তা সহজে বুজিবলৈ সহায় কৰে।",
            transliteration = "Spasta uccharane srotaxakolrok aaponar baarta xohaje bujiboloi xohai kore.",
            wordBreakdown = listOf(
                "Clear" to "স্পষ্ট (Spasta)",
                "pronunciation" to "উচ্চাৰণ (Uccharon)",
                "helps" to "সহায় কৰে (Xohai kore)",
                "listeners" to "শ্ৰোতাসকল (Srotaxakol)",
                "understand" to "বুজিবলৈ (Bujiboloi)",
                "your" to "আপোনাৰ (Aaponar)",
                "message" to "বাৰ্তা (Baarta)",
                "easily" to "সহজে (Xohaje)"
            )
        ),
        "we practice speaking english every day to build fluency" to TranslationResult(
            assameseText = "সাবলীলতা গঢ়ি তুলিবলৈ আমি প্ৰতিদিনে ইংৰাজী কোৱা অনুশীলন কৰোঁ।",
            transliteration = "Xaabolilota godhi tuliboloi aami protidine ingraji kowa anuxilon koru.",
            wordBreakdown = listOf(
                "We" to "আমি (Aami)",
                "practice" to "অনুশীলন কৰোঁ (Anuxilon koru)",
                "speaking" to "কোৱা (Kowa)",
                "English" to "ইংৰাজী (Ingraji)",
                "every day" to "প্ৰতিদিনে (Protidine)",
                "to build" to "গঢ়ি তুলিবলৈ (Godhi tuliboloi)",
                "fluency" to "সাবলীলতা (Xaabolilota)"
            )
        ),
        "collaborative conversations improve confidence in professional workplace settings" to TranslationResult(
            assameseText = "পাৰস্পৰিক কথোপকথনে পেছাদাৰী কৰ্মক্ষেত্ৰত আত্মবিশ্বাস বৃদ্ধি কৰে।",
            transliteration = "Paraxparik kothopokothone peshadari kormokhetrot aatmabishwakh briddhi kore.",
            wordBreakdown = listOf(
                "Collaborative" to "পাৰস্পৰিক (Paraxparik)",
                "conversations" to "কথোপকথন (Kothopokothon)",
                "improve" to "উন্নত / বৃদ্ধি কৰে (Briddhi kore)",
                "confidence" to "আত্মবিশ্বাস (Aatmabishwakh)",
                "in" to "-ত (In)",
                "professional" to "পেছাদাৰী (Peshadari)",
                "workplace" to "কৰ্মক্ষেত্ৰ (Kormokhetro)",
                "settings" to "পৰিবেশত (Poribexot)"
            )
        ),
        "i enjoy learning new vocabulary every day" to TranslationResult(
            assameseText = "মই প্ৰতিদিনে নতুন শব্দ শিকি ভাল পাওঁ।",
            transliteration = "Moi protidine notun xobdo xiki bhal pau.",
            wordBreakdown = listOf(
                "I" to "মই (Moi)",
                "enjoy" to "ভাল পাওঁ (Bhal pau)",
                "learning" to "শিকা (Xika)",
                "new" to "নতুন (Notun)",
                "vocabulary" to "শব্দভাণ্ডাৰ (Xobdobhandar)",
                "every day" to "প্ৰতিদিনে (Protidine)"
            )
        ),
        "practice makes a person perfect" to TranslationResult(
            assameseText = "অনুশীলনে মানুহক নিখুঁত কৰি তোলে।",
            transliteration = "Anuxilone manuhok nikhut kori tule.",
            wordBreakdown = listOf(
                "Practice" to "অনুশীলন (Anuxilon)",
                "makes" to "কৰি তোলে (Kori tule)",
                "a person" to "মানুহক (Manuhok)",
                "perfect" to "নিখুঁত (Nikhut)"
            )
        ),
        "reading books helps improve spoken language" to TranslationResult(
            assameseText = "কিতাপ পঢ়িলে কথিত ভাষাৰ উন্নতি হয়।",
            transliteration = "Kitap podhile kothito bhaxar unnati hoi.",
            wordBreakdown = listOf(
                "Reading" to "পঢ়া (Podha)",
                "books" to "কিতাপ (Kitap)",
                "helps" to "সহায় কৰে (Xohai kore)",
                "improve" to "উন্নতি কৰা (Unnati kora)",
                "spoken" to "কথিত (Kothito)",
                "language" to "ভাষা (Bhaxa)"
            )
        )
    )

    // Comprehensive English-to-Assamese bilingual dictionary
    private val wordMap = mapOf(
        // Core Pronouns & Articles
        "i" to Pair("মই", "Moi"),
        "we" to Pair("আমি", "Aami"),
        "you" to Pair("আপুনি / তুমি", "Aapuni / Tumi"),
        "he" to Pair("তেওঁ / সি", "Teo / Xi"),
        "she" to Pair("তাই / তেওঁ", "Tai / Teo"),
        "they" to Pair("তেওঁলোকে", "Teoloke"),
        "it" to Pair("ই / এইটো", "Ei / Eitu"),
        "this" to Pair("এই / এইটো", "Ei / Eitu"),
        "that" to Pair("সেই / সেইটো", "Xei / Xeitu"),
        "my" to Pair("মোৰ", "Moor"),
        "our" to Pair("আমাৰ", "Aamaar"),
        "your" to Pair("আপোনাৰ / তোমাৰ", "Aaponar / Tomar"),
        "his" to Pair("তেওঁৰ / তাৰ", "Teor / Taar"),
        "her" to Pair("তাইৰ / তেওঁৰ", "Taair / Teor"),
        "their" to Pair("তেওঁলোকৰ", "Teolokor"),

        // Language, Learning & Speech
        "interesting" to Pair("আকৰ্ষণীয় / আমোদজনক", "Aakorxoniyo / Aamodjonok"),
        "interest" to Pair("আগ্ৰহ / ৰুচি", "Aagroh / Rusi"),
        "interested" to Pair("আগ্ৰহী", "Aagrohi"),
        "speak" to Pair("কোৱা", "Kowa"),
        "speaks" to Pair("কয়", "Koy"),
        "speaking" to Pair("কোৱা / কথিত", "Kowa / Kothito"),
        "spoken" to Pair("কথিত / কোৱা", "Kothito"),
        "english" to Pair("ইংৰাজী", "Ingraji"),
        "language" to Pair("ভাষা", "Bhaxa"),
        "word" to Pair("শব্দ", "Xobdo"),
        "words" to Pair("শব্দবোৰ", "Xobdobor"),
        "sentence" to Pair("বাক্য", "Baakyo"),
        "sentences" to Pair("বাক্যবোৰ", "Baakyobor"),
        "practice" to Pair("অনুশীলন", "Anuxilon"),
        "learn" to Pair("শিকা", "Xika"),
        "learning" to Pair("শিকা / অধ্যয়ন", "Xika"),
        "listen" to Pair("শুনা", "Xuna"),
        "listening" to Pair("শুনা", "Xuna"),
        "pronounce" to Pair("উচ্চাৰণ কৰা", "Uccharon kora"),
        "pronunciation" to Pair("উচ্চাৰণ", "Uccharon"),
        "confidence" to Pair("আত্মবিশ্বাস", "Aatmabishwakh"),
        "confident" to Pair("আত্মবিশ্বাসী", "Aatmabishwaxi"),
        "fluency" to Pair("সাবলীলতা", "Xaabolilota"),
        "fluent" to Pair("সাবলীল", "Xaabolil"),
        "clarity" to Pair("স্পষ্টতা", "Spastata"),
        "clear" to Pair("স্পষ্ট", "Spasta"),
        "natural" to Pair("স্বাভাৱিক", "Swabhabik"),
        "vocabulary" to Pair("শব্দভাণ্ডাৰ", "Xobdobhandar"),
        "grammar" to Pair("ব্যাকৰণ", "Byakoron"),
        "accent" to Pair("উচ্চাৰণৰ টান", "Uccharonor taan"),
        "intonation" to Pair("সুৰৰ উঠা-নমা", "Suror utha-noma"),
        "syllable" to Pair("শব্দাংশ / দল", "Xobdangxo"),
        "stress" to Pair("জোৰ দিয়া / গুৰুত্ব", "Jor diya"),
        "dialogue" to Pair("সংলাপ / বাৰ্তালাপ", "Xonglap"),
        "conversation" to Pair("কথোপকথন", "Kothopokothon"),
        "talk" to Pair("কথা কোৱা", "Kotha kowa"),
        "chat" to Pair("কথোপকথন", "Kothopokothon"),
        "communication" to Pair("যোগাযোগ / ভাব-বিনিময়", "Jogajog"),
        "communicate" to Pair("যোগাযোগ কৰা", "Jogajog kora"),
        "meaning" to Pair("অৰ্থ", "Ortho"),
        "understand" to Pair("বুজা / বুজিব পৰা", "Buja"),
        "understanding" to Pair("বুজাবুজি / বোধ", "Bujabuji"),
        "phrase" to Pair("বাক্যাংশ", "Baakyangxo"),
        "alphabet" to Pair("বৰ্ণমালা", "Bornomala"),
        "voice" to Pair("মাত / কণ্ঠস্বৰ", "Maat / Konthoswor"),
        "sound" to Pair("শব্দ / ধ্বনি", "Dhwoni"),

        // Difficult / Phonetic Curated Words
        "comfortable" to Pair("আৰামদায়ক", "Aaramdayok"),
        "comfort" to Pair("আৰাম / সান্ত্বনা", "Aaram"),
        "thorough" to Pair("সম্পূৰ্ণ আৰু পুংখানুপুংখ", "Punkhanupunkho"),
        "specifically" to Pair("বিশেষভাৱে", "Bixexbhabe"),
        "specific" to Pair("নিৰ্দিষ্ট / বিশেষ", "Nirdishto"),
        "phenomenon" to Pair("পৰিঘটনা / বিস্ময়কৰ দৃশ্য", "Porigotona"),
        "entrepreneur" to Pair("উদ্যোগী / ব্যৱসায়ী", "Udyogi"),
        "worcestershire" to Pair("এক বিখ্যাত ইংৰাজ চছ", "Ingraj Sauce"),
        "schedule" to Pair("সময়সূচী", "Xomoysuchi"),
        "thoughtful" to Pair("চিন্তাশীল / বিবেচনাপূৰ্ণ", "Chintakhil"),
        "world" to Pair("পৃথিৱী / জগত", "Prithibi"),
        "hierarchy" to Pair("পদমৰ্যাদাৰ ক্ৰম", "Podomorjyadar Krom"),
        "colloquial" to Pair("কথিত / কথিত ভাষাৰ", "Kothito"),
        "resilience" to Pair("সহনশীলতা / স্থৈতিক ক্ষমতা", "Xohonxilota"),
        "anemone" to Pair("এবিধ ফুল বা সাগৰীয় জীৱ", "Xagoriya Jeeb"),
        "colonel" to Pair("কৰ্ণেল / উচ্চ সেনা বিষয়া", "Colonel"),
        "subtle" to Pair("সূক্ষ্ম / মৃদু", "Xukhmo"),
        "doubt" to Pair("সন্দেহ / দ্বিধা", "Xondeho"),
        "indict" to Pair("অভিযুক্ত কৰা", "Obhiyukto kora"),
        "island" to Pair("দ্বীপ", "Dweep"),
        "yacht" to Pair("বিলাসী নাও / জলযান", "Joljaan"),
        "recipe" to Pair("ৰন্ধন প্ৰণালী", "Rondhon Pronali"),
        "breath" to Pair("উশাহ / শ্বাস-প্ৰশ্বাস", "Uxaah"),
        "breathe" to Pair("উশাহ লোৱা", "Uxaah lowa"),
        "leather" to Pair("চামৰা", "Saamora"),
        "rhythm" to Pair("তাল / ছন্দ", "Taal / Sondo"),
        "collaborate" to Pair("সহযোগিতা কৰা", "Xohojogita kora"),
        "rural" to Pair("গ্ৰাম্য / গাঁৱলীয়া", "Gramyo"),
        "opportunity" to Pair("সুযোগ / সুযোগ-সুবিধা", "Xujog"),
        "important" to Pair("গুৰুত্বপূৰ্ণ / দৰকাৰী", "Gurutwopurno"),
        "importance" to Pair("গুৰুত্ব", "Gurutwo"),
        "education" to Pair("শিক্ষা", "Xikkha"),
        "knowledge" to Pair("জ্ঞান", "Gyaan"),
        "challenge" to Pair("প্ৰত্যাহ্বান", "Protyahban"),
        "beautiful" to Pair("ধুনীয়া / সুন্দৰ", "Dhuniya"),
        "technology" to Pair("প্ৰযুক্তিবিদ্যা / প্ৰযুক্তি", "Projukti"),
        "environment" to Pair("পৰিবেশ", "Poribex"),
        "experience" to Pair("অভিজ্ঞতা", "Obhiggota"),
        "difference" to Pair("পাৰ্থক্য", "Parthokyo"),
        "different" to Pair("বেলেগ / পৃথক", "Beleg / Prithok"),
        "difficult" to Pair("কঠিন / টান", "Kothin"),
        "difficulty" to Pair("অসুবিধা / কাঠিন্য", "Oxubidha"),
        "easy" to Pair("সহজ / সৰল", "Xohoj"),
        "easily" to Pair("সহজে", "Xohaje"),

        // Education & Campus
        "student" to Pair("ছাত্ৰ-ছাত্ৰী", "Shatro-shatri"),
        "teacher" to Pair("শিক্ষক / শিক্ষয়িত্ৰী", "Xikkhok"),
        "school" to Pair("বিদ্যালয়", "Bidyaloy"),
        "college" to Pair("মহাবিদ্যালয়", "Mohabidyaloy"),
        "university" to Pair("বিশ্ববিদ্যালয়", "Biswabidyaloy"),
        "classroom" to Pair("শ্ৰেণীকোঠা", "Srenikotha"),
        "class" to Pair("শ্ৰেণী", "Sreni"),
        "books" to Pair("কিতাপবোৰ", "Kitapbor"),
        "book" to Pair("কিতাপ / গ্ৰন্থ", "Kitap"),
        "library" to Pair("পুথিভঁৰাল", "Puthibhoral"),
        "read" to Pair("পঢ়া", "Podha"),
        "reading" to Pair("পঢ়া", "Podha"),
        "write" to Pair("লিখা", "Likha"),
        "writing" to Pair("লিখা", "Likha"),
        "study" to Pair("অধ্যয়ন / পঢ়া", "Odhyoyon"),
        "exam" to Pair("পৰীক্ষা", "Porikkha"),
        "examination" to Pair("পৰীক্ষা", "Porikkha"),
        "test" to Pair("পৰীক্ষা / পৰীক্ষণ", "Porikkha"),
        "result" to Pair("ফলাফল", "Pholafol"),
        "lesson" to Pair("পাঠ", "Paath"),
        "homework" to Pair("ঘৰুৱা কাম", "Ghoruwa kaam"),
        "question" to Pair("প্ৰশ্ন", "Proshno"),
        "answer" to Pair("উত্তৰ", "Uttor"),
        "problem" to Pair("সমস্যা", "Xomoxya"),
        "solution" to Pair("সমাধান", "Xomadhan"),

        // Everyday Nouns & Objects
        "water" to Pair("পানী / জল", "Paani"),
        "food" to Pair("খাদ্য / আহাৰ", "Khadya / Aahaar"),
        "tea" to Pair("চাহ", "Saah"),
        "coffee" to Pair("কফি", "Coffee"),
        "time" to Pair("সময়", "Xomoy"),
        "money" to Pair("টকা-পইচা", "Toka"),
        "work" to Pair("কাম / কাৰ্য", "Kaam"),
        "job" to Pair("চাকৰি / কাম", "Saakori"),
        "business" to Pair("ব্যৱসায়", "Byowoxay"),
        "home" to Pair("ঘৰ", "Ghor"),
        "house" to Pair("ঘৰ / গৃহ", "Ghor"),
        "room" to Pair("কোঠা", "Kotha"),
        "door" to Pair("দুৱাৰ", "Duwar"),
        "window" to Pair("খিৰিকী", "Khiriki"),
        "road" to Pair("ৰাস্তা / পথ", "Raasta / Poth"),
        "car" to Pair("গাড়ী", "Gaari"),
        "train" to Pair("ৰে'ল / ৰে'লগাড়ী", "Rail"),
        "bus" to Pair("বাছ", "Bus"),
        "airplane" to Pair("বিমান / উৰাজাহাজ", "Bimaan"),
        "city" to Pair("চহৰ / নগৰ", "Xohor"),
        "village" to Pair("গাঁও", "Gaon"),
        "country" to Pair("দেশ / ৰাষ্ট্ৰ", "Dex"),
        "people" to Pair("মানুহ / লোকসকল", "Manuh"),
        "person" to Pair("মানুহ / ব্যক্তি", "Byokti"),
        "man" to Pair("পুৰুষ / মানুহ", "Purux"),
        "woman" to Pair("মহিলা / তিৰোতা", "Mohila"),
        "child" to Pair("শিশু / ল'ৰা-ছোৱালী", "Xixu"),
        "children" to Pair("শিশুসকল", "Xixuxakol"),
        "friend" to Pair("বন্ধু / সখী", "Bondhu"),
        "friends" to Pair("বন্ধুসকল", "Bondhuxakol"),
        "family" to Pair("পৰিয়াল", "Poriyal"),
        "father" to Pair("পিতা / দেউতা", "Deuta"),
        "mother" to Pair("মাতৃ / মা", "Maa"),
        "brother" to Pair("ভাই / ককাই", "Bhai"),
        "sister" to Pair("ভনী / বাইদেউ", "Bhoni"),

        // Time Expressions
        "morning" to Pair("ৰাতিপুৱা / প্ৰভাত", "Raatipuwa"),
        "afternoon" to Pair("আবেলি / দুপৰীয়া", "Aabeli"),
        "evening" to Pair("গধূলি / সন্ধিয়া", "Godhuli"),
        "night" to Pair("ৰাতি / নিশা", "Raati"),
        "today" to Pair("আজি", "Aaji"),
        "tomorrow" to Pair("কাইলৈ", "Koiloi"),
        "yesterday" to Pair("কালি", "Kaali"),
        "daily" to Pair("দৈনন্দিন / প্ৰতিদিনে", "Doinondin"),
        "everyday" to Pair("প্ৰতিদিনে", "Protidine"),
        "always" to Pair("সদায়", "Xodai"),
        "never" to Pair("কেতিয়াও নহয়", "Ketiyao nohoi"),
        "sometimes" to Pair("কেতিয়াবা", "Ketiyaba"),
        "often" to Pair("প্ৰায়ে / সঘনাই", "Praaye"),
        "now" to Pair("এতিয়া", "Etiya"),
        "then" to Pair("তেতিয়া", "Tetiya"),
        "hour" to Pair("ঘণ্টা", "Ghonta"),
        "minute" to Pair("মিনিট", "Minute"),
        "second" to Pair("চেকেণ্ড / দ্বিতীয়", "Second"),
        "week" to Pair("সপ্তাহ", "Xoptaah"),
        "month" to Pair("মাহ", "Maah"),
        "year" to Pair("বছৰ", "Bosor"),

        // Common Verbs
        "help" to Pair("সহায়", "Xohai"),
        "helps" to Pair("সহায় কৰে", "Xohai kore"),
        "helping" to Pair("সহায় কৰা", "Xohai kora"),
        "think" to Pair("ভবা / চিন্তা কৰা", "Bhoba"),
        "know" to Pair("জনা / চিনাকি হোৱা", "Jona"),
        "remember" to Pair("মনত ৰখা", "Monot rokha"),
        "forget" to Pair("পাহৰা / পাহৰি যোৱা", "Pahora"),
        "see" to Pair("দেখা / চোৱা", "Dekha"),
        "look" to Pair("চোৱা", "Sowa"),
        "watch" to Pair("নিৰীক্ষণ কৰা / চোৱা", "Sowa"),
        "hear" to Pair("শুনা", "Xuna"),
        "go" to Pair("যোৱা", "Jowa"),
        "come" to Pair("অহা", "Oha"),
        "make" to Pair("বনোৱা / তৈয়াৰ কৰা", "Bonowa"),
        "do" to Pair("কৰা", "Kora"),
        "give" to Pair("দিয়া", "Diya"),
        "take" to Pair("লোৱা", "Lowa"),
        "buy" to Pair("কিনা / ক্ৰয় কৰা", "Kina"),
        "sell" to Pair("বিক্ৰী কৰা", "Bikri kora"),
        "start" to Pair("আৰম্ভ কৰা", "Aaromvo kora"),
        "finish" to Pair("শেষ কৰা", "Xex kora"),
        "stop" to Pair("ৰখা / বন্ধ কৰা", "Rokha"),
        "wait" to Pair("অপেক্ষা কৰা / ৰোৱা", "Rokha"),
        "try" to Pair("চেষ্টা কৰা", "Sesta kora"),
        "improve" to Pair("উন্নতি কৰা", "Unnoti kora"),
        "improves" to Pair("উন্নত কৰে", "Unnoto kore"),
        "build" to Pair("গঢ়ি তোলা / নিৰ্মাণ কৰা", "Godhi tula"),
        "live" to Pair("বাস কৰা / জীয়াই থকা", "Baax kora"),
        "love" to Pair("ভালপোৱা / মৰম", "Bhalpowa"),
        "like" to Pair("পছন্দ কৰা", "Poxondo kora"),

        // Common Adjectives & Values
        "good" to Pair("ভাল", "Bhal"),
        "great" to Pair("অতি ভাল / মহান", "Oti bhal"),
        "best" to Pair("সৰ্বশ্ৰেষ্ঠ / আটাইতকৈ ভাল", "Xorbosrestho"),
        "happy" to Pair("সুখী / আনন্দিত", "Xukhi"),
        "sad" to Pair("দুখীয়া / মন বেয়া", "Dukhi"),
        "big" to Pair("ডাঙৰ", "Daangor"),
        "small" to Pair("সৰু", "Xoru"),
        "fast" to Pair("দ্ৰুত / খৰকৈ", "Druto"),
        "slow" to Pair("ধীৰ / লাহে লাহে", "Lahe lahe"),
        "hot" to Pair("গৰম", "Gorom"),
        "cold" to Pair("ঠাণ্ডা", "Thanda"),
        "strong" to Pair("শক্তিশালী / বলৱান", "Xoktixali"),
        "weak" to Pair("দুৰ্বল", "Durbol"),
        "right" to Pair("শুদ্ধ / সোঁফাল", "Xuddho"),
        "wrong" to Pair("ভুল", "Bhul"),
        "true" to Pair("সঁচা / সত্য", "Xosa"),
        "false" to Pair("মিছা", "Misa"),
        "success" to Pair("সাফল্য", "Xafolyo"),
        "successful" to Pair("সফল", "Xofol"),
        "failure" to Pair("ব্যৰ্থতা", "Byorthota"),
        "courage" to Pair("সাহস", "Xahox"),
        "patience" to Pair("ধৈৰ্য", "Dhoirjo"),
        "honesty" to Pair("সততা", "Xotota"),
        "truth" to Pair("সত্য", "Xotyo"),
        "peace" to Pair("শান্তি", "Xaanti"),
        "freedom" to Pair("স্বাধীনতা", "Xwadhinota"),
        "health" to Pair("স্বাস্থ্য", "Xwasthyo"),
        "healthy" to Pair("সুস্থ / স্বাস্থ্যৱান", "Xustho"),
        "clean" to Pair("পৰিষ্কাৰ / নিকা", "Poriskar"),
        "new" to Pair("নতুন", "Notun"),
        "old" to Pair("পুৰণি / বয়সীয়াল", "Puroni"),

        // Interrogatives & Polite Phrases
        "who" to Pair("কোন", "Kun"),
        "what" to Pair("কি", "Ki"),
        "where" to Pair("ক'ত", "Kot"),
        "when" to Pair("কেতিয়া", "Ketiya"),
        "why" to Pair("কিয়", "Kiyo"),
        "how" to Pair("কেনেকৈ", "Kenekoi"),
        "which" to Pair("কোনটো", "Kuntu"),
        "please" to Pair("অনুগ্ৰহ কৰি", "Onugroh kori"),
        "thank" to Pair("ধন্যবাদ", "Dhonyobaad"),
        "thanks" to Pair("ধন্যবাদ", "Dhonyobaad"),
        "welcome" to Pair("স্বাগতম", "Xwagotom"),
        "sorry" to Pair("ক্ষমা কৰিব", "Khyoma koribo"),
        "hello" to Pair("নমস্কাৰ", "Nomoxkar"),
        "congratulations" to Pair("অভিনন্দন", "Obhinondon"),

        // Body, Health & Wellness
        "head" to Pair("মূৰ", "Mur"),
        "eye" to Pair("চকু", "Soku"),
        "eyes" to Pair("চকুযুৰি", "Sokujuri"),
        "ear" to Pair("কাণ", "Kaan"),
        "nose" to Pair("নাক", "Naak"),
        "mouth" to Pair("মুখ", "Mukh"),
        "hand" to Pair("হাত", "Haat"),
        "hands" to Pair("হাত দুখন", "Haat dukhon"),
        "leg" to Pair("ভৰি", "Bhori"),
        "heart" to Pair("হৃদয় / অন্তৰ", "Hridoy"),
        "brain" to Pair("মগজু / মস্তিষ্ক", "Mogoju"),
        "mind" to Pair("মন", "Mon"),
        "body" to Pair("শৰীৰ / দেহ", "Xorir"),
        "doctor" to Pair("চিকিৎসক / ডাক্তৰ", "Dactor"),
        "hospital" to Pair("চিকিৎসালয়", "Sikitsaloy"),
        "medicine" to Pair("ঔষধ / দৰৱ", "Ouxodh"),

        // Nature, Earth & Weather
        "nature" to Pair("প্ৰকৃতি", "Prokriti"),
        "sun" to Pair("সূৰ্য্য / বেলি", "Xurjo"),
        "moon" to Pair("জোন / চন্দ্ৰ", "Jon"),
        "star" to Pair("তৰা / নক্ষত্ৰ", "Tora"),
        "sky" to Pair("আকাশ", "Aakax"),
        "river" to Pair("নদী / নৈ", "Nodi"),
        "sea" to Pair("সাগৰ / সমুদ্ৰ", "Xagor"),
        "ocean" to Pair("মহাসাগৰ", "Moxagor"),
        "mountain" to Pair("পাহাৰ / পৰ্বত", "Pahar"),
        "forest" to Pair("অৰণ্য / হাবনি", "Oronyo"),
        "tree" to Pair("গছ", "Gos"),
        "flower" to Pair("ফুল", "Phul"),
        "rain" to Pair("বৰষুণ", "Boroqun"),
        "wind" to Pair("বতাহ", "Botah"),
        "fire" to Pair("জুই / অগ্নি", "Jui"),
        "air" to Pair("বায়ু / বতাহ", "Bayu"),
        "earth" to Pair("পৃথিৱী / মাটি", "Prithibi"),
        "weather" to Pair("বতৰ", "Botor"),
        "cloud" to Pair("ডাৱৰ / মেঘ", "Dabor"),
        "summer" to Pair("গ্ৰীষ্মকাল", "Grishmokal"),
        "winter" to Pair("শীতকাল", "Xitkal"),

        // Animals & Birds
        "animal" to Pair("পশু / জীৱ-জন্তু", "Poxu"),
        "bird" to Pair("চৰাই / পক্ষী", "Sorai"),
        "dog" to Pair("কুকুৰ", "Kukur"),
        "cat" to Pair("মেকুৰী", "Mekuri"),
        "cow" to Pair("গৰু", "Goru"),
        "elephant" to Pair("হাতী / গজ", "Haati"),
        "horse" to Pair("ঘোঁৰা", "Ghora"),
        "lion" to Pair("সিংহ", "Xingho"),
        "tiger" to Pair("বাঘ", "Baagh"),
        "fish" to Pair("মাছ / মৎস্য", "Maas"),

        // Food, Flavors & Cooking
        "rice" to Pair("ভাত / চাউল", "Bhaat"),
        "bread" to Pair("ৰুটী", "Ruti"),
        "milk" to Pair("গাখীৰ", "Gaakhir"),
        "fruit" to Pair("ফল / ফলমূল", "Phol"),
        "apple" to Pair("আপেল", "Aapel"),
        "banana" to Pair("কল", "Kol"),
        "vegetable" to Pair("শাক-পাচলি", "Xaak-pasoli"),
        "salt" to Pair("নিমখ", "Nimokh"),
        "sugar" to Pair("চেনি", "Seni"),
        "sweet" to Pair("মিঠা", "Mitha"),
        "sour" to Pair("টেঙা", "Tenga"),
        "spicy" to Pair("জ্বাল / মচলাযুক্ত", "Jwal"),
        "delicious" to Pair("সুস্বাদু / সোৱাদযুক্ত", "Xuxwadu"),
        "eat" to Pair("খোৱা / আহাৰ গ্ৰহণ", "Khowa"),
        "drink" to Pair("পান কৰা / খোৱা", "Paan kora"),
        "cook" to Pair("ৰন্ধা / ৰন্ধন", "Rondha"),

        // Daily Routine & Action Verbs
        "walk" to Pair("খোজ কঢ়া", "Khoj kodha"),
        "run" to Pair("দৌৰা", "Doura"),
        "jump" to Pair("জপিয়াই উঠা", "Jopiyai utha"),
        "sleep" to Pair("শোৱা / টোপনি", "Xowa"),
        "wake" to Pair("সাৰ পোৱা", "Xaar powa"),
        "laugh" to Pair("হঁহা / আনন্দ প্ৰকাশ", "Hoha"),
        "cry" to Pair("কন্দা", "Konda"),
        "smile" to Pair("হাঁহি / মৃদু হাঁহি", "Hahi"),
        "play" to Pair("খেলা / আনন্দ কৰা", "Khela"),
        "sing" to Pair("গান গোৱা", "Gaan gowa"),
        "dance" to Pair("নাচা / নৃত্য কৰা", "Naasa"),
        "drive" to Pair("গাড়ী চলোৱা", "Gaari solowa"),
        "fly" to Pair("উৰা", "Ura"),
        "swim" to Pair("সাঁতোৰা", "Xatora"),
        "ask" to Pair("সোধা / প্ৰশ্ন কৰা", "Xodha"),
        "tell" to Pair("কোৱা / জনোৱা", "Kowa"),
        "call" to Pair("মতা / ফোন কৰা", "Mota"),
        "send" to Pair("পঠোৱা / প্ৰেৰণ কৰা", "Pothowa"),
        "receive" to Pair("গ্ৰহণ কৰা / পোৱা", "Grohon kora"),
        "open" to Pair("খোলা / মুকলি কৰা", "Khula"),
        "close" to Pair("বন্ধ কৰা", "Bondho kora"),
        "sit" to Pair("বহা", "Boha"),
        "stand" to Pair("থিয় হোৱা", "Thiyo howa"),
        "meet" to Pair("লগ পোৱা / সাক্ষাৎ কৰা", "Log powa"),
        "teach" to Pair("শিকোৱা / শিক্ষা দিয়া", "Xikowa"),
        "guide" to Pair("পথ প্ৰদৰ্শন কৰা", "Poth prodorxon kora"),

        // Emotions, Personality & Values
        "hope" to Pair("আশা / প্ৰত্যাশা", "Aaxa"),
        "dream" to Pair("সপোন", "Xopon"),
        "wish" to Pair("ইচ্ছা / কামনা", "Iccha"),
        "trust" to Pair("বিশ্বাস / আস্থা", "Biswaax"),
        "respect" to Pair("শ্ৰদ্ধা / সন্মান", "Xroddha"),
        "kindness" to Pair("দয়া / সহৃদয়তা", "Doya"),
        "joy" to Pair("আনন্দ / উল্লাস", "Aanondo"),
        "anger" to Pair("খং / ক্ৰোধ", "Khong"),
        "fear" to Pair("ভয় / শংকা", "Bhoy"),
        "curiosity" to Pair("কৌতূহল / জানিবলৈ ইচ্ছা", "Koutuhol"),
        "patience" to Pair("ধৈৰ্য / সহনশীলতা", "Dhoirjo"),
        "proud" to Pair("গৰ্বিত", "Gorbito"),
        "pride" to Pair("গৌৰৱ / অহংকাৰ", "Gourov"),
        "brave" to Pair("সাহসী / নিৰ্ভীক", "Xahoxi"),
        "honest" to Pair("সৎ / নিষ্ঠাবান", "Xot"),

        // Technology, Career & Academic
        "computer" to Pair("কম্পিউটাৰ / গণক যন্ত্ৰ", "Computer"),
        "internet" to Pair("ইণ্টাৰনেট", "Internet"),
        "software" to Pair("ছফ্টৱেৰ", "Software"),
        "phone" to Pair("ফোন / দূৰভাষ", "Phone"),
        "screen" to Pair("পৰ্দা / স্ক্ৰীণ", "Porda"),
        "data" to Pair("তথ্য / উপাত্ত", "Tothyo"),
        "network" to Pair("নেটৱৰ্ক / যোগাযোগ জাল", "Network"),
        "security" to Pair("নিৰাপত্তা / সুৰক্ষা", "Niroppotta"),
        "system" to Pair("ব্যৱস্থা / পদ্ধতি", "Byowostha"),
        "science" to Pair("বিজ্ঞান", "Bigyan"),
        "history" to Pair("ইতিহাস", "Itihax"),
        "geography" to Pair("ভূগোল", "Bhugol"),
        "mathematics" to Pair("গণিত", "Gonit"),
        "culture" to Pair("সংস্কৃতি", "Xongskriti"),
        "society" to Pair("সমাজ", "Xomaj"),
        "government" to Pair("চৰকাৰ / প্ৰশাসন", "Xorkar"),
        "policy" to Pair("নীতি / নিয়মাৱলী", "Niti"),
        "goal" to Pair("লক্ষ্য / উদ্দেশ্য", "Lokkhyo"),
        "progress" to Pair("উন্নতি / অগ্ৰগতি", "Unnoti"),
        "project" to Pair("প্ৰকল্প", "Prokolpo"),
        "strategy" to Pair("কৌশল / পৰিকল্পনা", "Kouxol"),
        "decision" to Pair("সিদ্ধান্ত", "Xiddhanto"),
        "talent" to Pair("প্ৰতিভা / গুণ", "Protibha"),
        "effort" to Pair("প্ৰচেষ্টা / শ্ৰম", "Prosesta"),
        "skill" to Pair("দক্ষতা / নিপুণতা", "Dokkhota"),
        "leader" to Pair("নেতা / দলপতি", "Neta"),
        "leadership" to Pair("নেতৃত্ব", "Netritwo"),
        "team" to Pair("দল / গোট", "Dol"),
        "future" to Pair("ভৱিষ্যত", "Bhobisyot"),
        "present" to Pair("বৰ্তমান / উপহাৰ", "Bortoman"),
        "past" to Pair("অতীত", "Otit"),

        // Descriptors & Adverbs
        "wonderful" to Pair("আশ্চৰ্য্যজনক / সুন্দৰ", "Aasorjojonok"),
        "awesome" to Pair("অসাধাৰণ / অতি চমৎকার", "Oxadharon"),
        "perfect" to Pair("নিখুঁত / সম্পূৰ্ণ শুদ্ধ", "Nikhut"),
        "amazing" to Pair("চমৎকাৰ / বিস্ময়কৰ", "Somotkar"),
        "bright" to Pair("উজ্জ্বল / দীপ্তিমান", "Ujjwol"),
        "dark" to Pair("আন্ধাৰ / অন্ধকাৰ", "Aandhar"),
        "heavy" to Pair("গধূৰ / ভাৰী", "Godhur"),
        "light" to Pair("পাতল / পোহৰ", "Patol"),
        "deep" to Pair("গভীৰ", "Gobhir"),
        "wide" to Pair("বহল / প্ৰশস্ত", "Bohol"),
        "narrow" to Pair("ঠেক / সংকীৰ্ণ", "Thek"),
        "high" to Pair("ওখ / উচ্চ", "Okh"),
        "low" to Pair("চাপৰ / নিম্ন", "Saapor"),
        "near" to Pair("ওচৰত / কাষত", "Osrot"),
        "far" to Pair("দূৰত", "Durot"),
        "early" to Pair("সোণকালে", "Xunkale"),
        "late" to Pair("পলমকৈ", "Polomkoi"),
        "quick" to Pair("খৰকৈ / ত্বৰিৎ", "Khorkoi"),
        "quiet" to Pair("শান্ত / মৌন", "Xaanto"),
        "loud" to Pair("উচ্চ / জোৰেৰে", "Usso"),
        "rich" to Pair("ধনী / ঐশ্বৰ্যশালী", "Dhoni"),
        "poor" to Pair("দুখীয়া / নিচলা", "Dukhiya"),
        "clean" to Pair("পৰিষ্কাৰ / নিকা", "Poriskar"),
        "actually" to Pair("প্ৰকৃততে / আচলতে", "Prokritote"),
        "really" to Pair("সঁচাকৈ / বাস্তৱত", "Xosakoi"),
        "certainly" to Pair("নিশ্চয় / নিশ্চিতভাৱে", "Nischoy"),
        "probably" to Pair("সম্ভৱতঃ", "Xombhoboto"),
        "together" to Pair("একত্ৰিতভাৱে / একেলগে", "Ekeloge"),
        "alone" to Pair("অকলশৰীয়া", "Okolxoriya"),
        "everyone" to Pair("সকলোৱে / প্ৰতিজন ব্যক্তি", "Xokolowe"),
        "everything" to Pair("সকলোবোৰ / সৰ্বস্ব", "Xokolobor")
    )

    // Dynamic Teacher-managed custom words storage
    private val teacherCustomWords = java.util.concurrent.ConcurrentHashMap<String, Pair<String, String>>()

    fun addCustomWord(word: String, assameseMeaning: String, transliteration: String) {
        val clean = word.trim().lowercase()
        if (clean.isNotBlank()) {
            teacherCustomWords[clean] = Pair(assameseMeaning.trim(), transliteration.trim())
        }
    }

    fun removeCustomWord(word: String) {
        teacherCustomWords.remove(word.trim().lowercase())
    }

    fun getAllCustomWords(): Map<String, Pair<String, String>> = teacherCustomWords.toMap()

    fun getTotalWordCount(): Int = wordMap.size + teacherCustomWords.size

    /**
     * Translates the given English sentence to Assamese.
     * Uses custom Assamese text if provided, otherwise looks up curated sentences,
     * or dynamically constructs a bilingual translation with pronunciation and word-by-word bridge.
     */
    fun translate(englishSentence: String, customAssamese: String? = null): TranslationResult {
        if (!customAssamese.isNullOrBlank()) {
            val breakdown = generateBreakdown(englishSentence)
            return TranslationResult(
                assameseText = customAssamese.trim(),
                transliteration = "অসমীয়া অৰ্থ (Assamese Meaning)",
                wordBreakdown = breakdown
            )
        }

        val normalized = englishSentence.trim().lowercase()
            .replace(".", "")
            .replace(",", "")
            .replace("!", "")
            .replace("?", "")

        curatedSentences[normalized]?.let { return it }

        // Dynamic synthesis based on vocabulary
        val tokens = englishSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        val breakdown = mutableListOf<Pair<String, String>>()
        val assameseWords = mutableListOf<String>()
        val transliterationWords = mutableListOf<String>()

        for (token in tokens) {
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            val match = translateWord(clean)
            if (match != null) {
                assameseWords.add(match.first)
                transliterationWords.add(match.second)
                breakdown.add(token to "${match.first} (${match.second})")
            } else {
                assameseWords.add(token)
                transliterationWords.add(token)
                breakdown.add(token to token)
            }
        }

        val assameseText = if (assameseWords.isNotEmpty()) {
            "${assameseWords.joinToString(" ")}।"
        } else {
            "অসমীয়া অনুবাদ উপলভ্য।"
        }

        val transliteration = transliterationWords.joinToString(" ")

        return TranslationResult(
            assameseText = assameseText,
            transliteration = transliteration,
            wordBreakdown = breakdown
        )
    }

    private fun generateBreakdown(englishSentence: String): List<Pair<String, String>> {
        val tokens = englishSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        val breakdown = mutableListOf<Pair<String, String>>()
        for (token in tokens) {
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            val match = translateWord(clean)
            if (match != null) {
                breakdown.add(token to "${match.first} (${match.second})")
            } else {
                breakdown.add(token to token)
            }
        }
        return breakdown
    }

    /**
     * Translates an individual English word to Assamese.
     * Uses direct dictionary lookup, morphological root stemming,
     * and fallback phonetic transliteration so ANY queried word returns a meaningful result.
     */
    fun translateWord(word: String): Pair<String, String>? {
        val clean = word.lowercase().trim().replace(Regex("[^a-z]"), "")
        if (clean.isEmpty()) return null

        // 0. Check teacher-added custom words first
        teacherCustomWords[clean]?.let { return it }

        // 1. Direct dictionary lookup
        wordMap[clean]?.let { return it }

        // 2. Morphological root stemming heuristics
        // -ing
        if (clean.endsWith("ing") && clean.length > 4) {
            val stem1 = clean.removeSuffix("ing")
            wordMap[stem1]?.let { return it }
            val stem2 = "${stem1}e"
            wordMap[stem2]?.let { return it }
            if (stem1.length >= 2 && stem1.last() == stem1[stem1.length - 2]) {
                wordMap[stem1.dropLast(1)]?.let { return it } // running -> run
            }
        }

        // -ed
        if (clean.endsWith("ed") && clean.length > 3) {
            val stem1 = clean.removeSuffix("ed")
            wordMap[stem1]?.let { return it }
            val stem2 = "${stem1}e"
            wordMap[stem2]?.let { return it }
            if (clean.endsWith("ied")) {
                wordMap["${clean.removeSuffix("ied")}y"]?.let { return it }
            }
        }

        // -ly
        if (clean.endsWith("ly") && clean.length > 3) {
            val stem1 = clean.removeSuffix("ly")
            wordMap[stem1]?.let { return it }
            if (clean.endsWith("ily")) {
                wordMap["${clean.removeSuffix("ily")}y"]?.let { return it }
            }
        }

        // -s / -es
        if (clean.endsWith("ies") && clean.length > 4) {
            wordMap["${clean.removeSuffix("ies")}y"]?.let { return it }
        }
        if (clean.endsWith("es") && clean.length > 3) {
            wordMap[clean.removeSuffix("es")]?.let { return it }
        }
        if (clean.endsWith("s") && clean.length > 2) {
            wordMap[clean.removeSuffix("s")]?.let { return it }
        }

        // -tion / -ment / -able
        if (clean.endsWith("tion") && clean.length > 5) {
            wordMap["${clean.removeSuffix("tion")}te"]?.let { return it }
        }
        if (clean.endsWith("ment") && clean.length > 5) {
            wordMap[clean.removeSuffix("ment")]?.let { return it }
        }
        if (clean.endsWith("able") && clean.length > 5) {
            wordMap[clean.removeSuffix("able")]?.let { return it }
        }

        // 3. Phonetic transliteration into Assamese script so students see the word in Assamese phonetics
        return transliterateToAssamese(clean)
    }

    /**
     * Converts an English word into natural Assamese phonetic script representation.
     */
    private fun transliterateToAssamese(word: String): Pair<String, String> {
        val sb = StringBuilder()
        var i = 0
        while (i < word.length) {
            val remaining = word.substring(i)
            when {
                remaining.startsWith("th") -> { sb.append("থ"); i += 2 }
                remaining.startsWith("ch") -> { sb.append("চ"); i += 2 }
                remaining.startsWith("sh") -> { sb.append("শ"); i += 2 }
                remaining.startsWith("ph") -> { sb.append("ফ"); i += 2 }
                remaining.startsWith("wh") -> { sb.append("উ"); i += 2 }
                remaining.startsWith("ck") -> { sb.append("ক"); i += 2 }
                remaining.startsWith("ng") -> { sb.append("ং"); i += 2 }
                remaining.startsWith("qu") -> { sb.append("কু"); i += 2 }
                remaining.startsWith("ee") || remaining.startsWith("ea") -> { sb.append("ী"); i += 2 }
                remaining.startsWith("oo") -> { sb.append("ু"); i += 2 }
                remaining.startsWith("ai") || remaining.startsWith("ay") -> { sb.append("াই"); i += 2 }
                remaining.startsWith("ou") || remaining.startsWith("ow") -> { sb.append("াউ"); i += 2 }
                else -> {
                    when (word[i]) {
                        'b' -> sb.append("ব")
                        'c' -> sb.append(if (i + 1 < word.length && "eiy".contains(word[i + 1])) "চ" else "ক")
                        'd' -> sb.append("ড")
                        'f' -> sb.append("ফ")
                        'g' -> sb.append("গ")
                        'h' -> sb.append("হ")
                        'j' -> sb.append("জ")
                        'k' -> sb.append("ক")
                        'l' -> sb.append("ল")
                        'm' -> sb.append("ম")
                        'n' -> sb.append("ন")
                        'p' -> sb.append("প")
                        'r' -> sb.append("ৰ")
                        's' -> sb.append("ছ")
                        't' -> sb.append("ট")
                        'v' -> sb.append("ভ")
                        'w' -> sb.append("ৱ")
                        'x' -> sb.append("ক্স")
                        'y' -> sb.append("য়")
                        'z' -> sb.append("জ")
                        'a' -> sb.append(if (i == 0) "আ" else "া")
                        'e' -> sb.append(if (i == 0) "এ" else "ে")
                        'i' -> sb.append(if (i == 0) "ই" else "ি")
                        'o' -> sb.append(if (i == 0) "অ" else "ো")
                        'u' -> sb.append(if (i == 0) "উ" else "ু")
                        else -> sb.append(word[i])
                    }
                    i++
                }
            }
        }
        val capitalized = word.replaceFirstChar { it.uppercase() }
        val assamesePhonetic = sb.toString()
        return Pair(assamesePhonetic, capitalized)
    }
}
