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

    // English-to-Assamese bilingual dictionary
    private val wordMap = mapOf(
        "i" to Pair("মই", "Moi"),
        "we" to Pair("আমি", "Aami"),
        "you" to Pair("আপুনি / তুমি", "Aapuni / Tumi"),
        "he" to Pair("তেওঁ / সি", "Teo / Xi"),
        "she" to Pair("তাই / তেওঁ", "Tai / Teo"),
        "they" to Pair("তেওঁলোকে", "Teoloke"),
        "speak" to Pair("কোৱা", "Kowa"),
        "speaks" to Pair("কয়", "Koy"),
        "speaking" to Pair("কোৱা", "Kowa"),
        "english" to Pair("ইংৰাজী", "Ingraji"),
        "language" to Pair("ভাষা", "Bhaxa"),
        "words" to Pair("শব্দবোৰ", "Xobdobor"),
        "word" to Pair("শব্দ", "Xobdo"),
        "sentence" to Pair("বাক্য", "Baakyo"),
        "sentences" to Pair("বাক্যবোৰ", "Baakyobor"),
        "practice" to Pair("অনুশীলন", "Anuxilon"),
        "learn" to Pair("শিকা", "Xika"),
        "learning" to Pair("শিকি থকা", "Xiki thoka"),
        "listen" to Pair("শুনা", "Xuna"),
        "pronunciation" to Pair("উচ্চাৰণ", "Uccharon"),
        "confidence" to Pair("আত্মবিশ্বাস", "Aatmabishwakh"),
        "fluency" to Pair("সাবলীলতা", "Xaabolilota"),
        "clarity" to Pair("স্পষ্টতা", "Spastata"),
        "clear" to Pair("স্পষ্ট", "Spasta"),
        "natural" to Pair("স্বাভাৱিক", "Swabhabik"),
        "easily" to Pair("সহজে", "Xohaje"),
        "daily" to Pair("দৈনন্দিন", "Doinondin"),
        "every" to Pair("প্ৰতি", "Proti"),
        "day" to Pair("দিন", "Din"),
        "good" to Pair("ভাল", "Bhal"),
        "great" to Pair("অতি ভাল", "Oti bhal"),
        "with" to Pair("সৈতে / -ৰে", "Soite"),
        "and" to Pair("আৰু", "Aaru"),
        "in" to Pair("-ত", "In"),
        "to" to Pair("-লৈ", "Loi"),
        "for" to Pair("বাবে", "Babe"),
        "helps" to Pair("সহায় কৰে", "Xohai kore"),
        "improves" to Pair("উন্নত কৰে", "Unnoto kore"),
        "improve" to Pair("উন্নতি কৰা", "Unnoti kora"),
        "read" to Pair("পঢ়া", "Podha"),
        "reads" to Pair("পঢ়ে", "Podhe"),
        "books" to Pair("কিতাপ", "Kitap"),
        "student" to Pair("ছাত্ৰ-ছাত্ৰী", "Shatro-shatri"),
        "teacher" to Pair("শিক্ষক", "Xikkhok"),
        "class" to Pair("শ্ৰেণী", "Sreni"),
        "school" to Pair("বিদ্যালয়", "Bidyaloy")
    )

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
            val match = wordMap[clean]
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
            val match = wordMap[clean]
            if (match != null) {
                breakdown.add(token to "${match.first} (${match.second})")
            } else {
                breakdown.add(token to token)
            }
        }
        return breakdown
    }
}
