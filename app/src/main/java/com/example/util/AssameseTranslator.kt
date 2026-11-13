package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

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
        ),
        // Common Greetings & Introductions
        "hello how are you" to TranslationResult(
            assameseText = "নমস্কাৰ! আপুনি কেনে আছে?",
            transliteration = "Nomoxkar! Aapuni kene aase?",
            wordBreakdown = listOf("Hello" to "নমস্কাৰ (Nomoxkar)", "How" to "কেনে (Kene)", "Are you" to "আপুনি আছে (Aapuni aase)")
        ),
        "hello" to TranslationResult(
            assameseText = "নমস্কাৰ!",
            transliteration = "Nomoxkar!",
            wordBreakdown = listOf("Hello" to "নমস্কাৰ (Nomoxkar)")
        ),
        "hi" to TranslationResult(
            assameseText = "নমস্কাৰ!",
            transliteration = "Nomoxkar!",
            wordBreakdown = listOf("Hi" to "নমস্কাৰ (Nomoxkar)")
        ),
        "how are you" to TranslationResult(
            assameseText = "আপুনি কেনে আছে? / তোমাৰ খবৰ কি?",
            transliteration = "Aapuni kene aase? / Tomar khobor ki?",
            wordBreakdown = listOf("How" to "কেনে (Kene)", "Are you" to "আপুনি আছে (Aapuni aase)")
        ),
        "hi how are you" to TranslationResult(
            assameseText = "নমস্কাৰ! আপুনি কেনে আছে?",
            transliteration = "Nomoxkar! Aapuni kene aase?",
            wordBreakdown = listOf("Hi" to "নমস্কাৰ (Nomoxkar)", "How are you" to "আপুনি কেনে আছে? (Aapuni kene aase?)")
        ),
        "hello how are you doing" to TranslationResult(
            assameseText = "নমস্কাৰ! আপোনাৰ দিনটো কেনে চলিছে?",
            transliteration = "Nomoxkar! Aaponar dinto kene solise?",
            wordBreakdown = listOf("Hello" to "নমস্কাৰ (Nomoxkar)", "How are you doing" to "আপোনাৰ কেনে চলিছে (Aaponar kene solise)")
        ),
        "tell me about yourself" to TranslationResult(
            assameseText = "আপোনাৰ বিষয়ে কওক।",
            transliteration = "Aaponar bikhoye kouk.",
            wordBreakdown = listOf("Tell" to "কওক (Kouk)", "Me" to "মোক (Mook)", "About" to "বিষয়ে (Bikhoye)", "Yourself" to "আপোনাৰ (Aaponar)")
        ),
        "when do you usually go to school" to TranslationResult(
            assameseText = "তুমি সাধাৰণতে কেতিয়া স্কুললৈ যোৱা?",
            transliteration = "Tumi sadharonote ketiya school-loi jowa?",
            wordBreakdown = listOf("When" to "কেতিয়া (Ketiya)", "Usually" to "সাধাৰণতে (Sadharonote)", "Go to school" to "স্কুললৈ যোৱা (School-loi jowa)")
        ),
        "when do you usually wake up on weekdays" to TranslationResult(
            assameseText = "সপ্তাহৰ কামৰ দিনবোৰত তুমি সাধাৰণতে কেতিয়া সাৰ পোৱা?",
            transliteration = "Soptahor kormodinborot tumi sadharonote ketiya xar powa?",
            wordBreakdown = listOf("When" to "কেতিয়া (Ketiya)", "Wake up" to "সাৰ পোৱা (Xar powa)", "Weekdays" to "কৰ্মদিনবোৰত (Kormodinborot)")
        ),
        "what time do you wake up in the morning" to TranslationResult(
            assameseText = "তুমি ৰাতিপুৱা কেইবজাত সাৰ পোৱা?",
            transliteration = "Tumi raatipuwa keibojat xar powa?",
            wordBreakdown = listOf("What time" to "কেইবজাত (Keibojat)", "Morning" to "ৰাতিপুৱা (Raatipuwa)", "Wake up" to "সাৰ পোৱা (Xar powa)")
        ),
        "what activities do you like doing on sundays" to TranslationResult(
            assameseText = "দেওবাৰে তুমি কি কি কাম কৰিবলৈ ভাল পোৱা?",
            transliteration = "Deobare tumi ki ki kaam koriboloi bhal powa?",
            wordBreakdown = listOf("Activities" to "কামবোৰ (Kaambor)", "Sundays" to "দেওবাৰে (Deobare)", "Like doing" to "কৰি ভাল পোৱা (Kori bhal powa)")
        ),
        "in which month is your birthday and what do you do on that day" to TranslationResult(
            assameseText = "তোমাৰ জন্মদিন কোন মাহত পৰে, আৰু সেইদিনা তুমি কি কৰা?",
            transliteration = "Tomar jonmodin kun mahot pore, aaru xeidina tumi ki kora?",
            wordBreakdown = listOf("Which month" to "কোন মাহত (Kun mahot)", "Birthday" to "জন্মদিন (Jonmodin)", "What do you do" to "তুমি কি কৰা (Tumi ki kora)")
        ),
        "where will you be at midnight on new years eve" to TranslationResult(
            assameseText = "নতুন বছৰৰ নিশা ১২ বজাত তুমি ক'ত থাকিবা?",
            transliteration = "Notun bosonor nixa 12 bojat tumi kot thakiba?",
            wordBreakdown = listOf("Where" to "ক'ত (Kot)", "Midnight" to "নিশা ১২ বজাত (Nixa 12 bojat)", "New Year's Eve" to "নতুন বছৰৰ পূৰ্বসন্ধ্যা (Notun bosonor purboxondhya)")
        ),
        "could you tell me about your favorite hobbies and why you enjoy spending time on them" to TranslationResult(
            assameseText = "তোমাৰ প্ৰিয় চখবোৰৰ বিষয়ে আৰু সেইবোৰত সময় কটাই কিয় ভাল পোৱা কওকচোন।",
            transliteration = "Tomar priyo sokhboror bikhoye aaru xeiborot xomoi kotai kiyo bhal powa koukson.",
            wordBreakdown = listOf("Hobbies" to "চখবোৰ (Sokhbor)", "Favorite" to "প্ৰিয় (Priyo)", "Enjoy" to "ভাল পোৱা (Bhal powa)")
        ),
        "what kind of job or profession do you do and what do you like most about your work or studies" to TranslationResult(
            assameseText = "আপুনি কি বৃত্তি বা কাম কৰে, আৰু আপোনাৰ কৰ্ম বা পঢ়াশুনাত আটাইতকৈ কি ভাল লাগে?",
            transliteration = "Aapuni ki britti ba kaam kore, aaru aaponar kormo ba podhaxunat aataitkoi ki bhal lage?",
            wordBreakdown = listOf("Job/Profession" to "বৃত্তি/কাম (Britti/Kaam)", "Work/Studies" to "কৰ্ম/পঢ়াশুনা (Kormo/Podhaxuna)")
        ),
        "if you could travel anywhere in the world tomorrow with all expenses paid where would you go and why" to TranslationResult(
            assameseText = "যদি কাইলৈ বিনামূলীয়াকৈ বিশ্বৰ যিকোনো ঠাইলৈ ভ্ৰমণৰ সুযোগ পায়, আপুনি ক'লৈ যাব আৰু কিয়?",
            transliteration = "Jodi kailoi binamuliyakoi biswor jikuno thailoi bhromonor xujog pay, aapuni koloi jaabo aaru kiyo?",
            wordBreakdown = listOf("Travel" to "ভ্ৰমণ কৰা (Bhromon kora)", "Anywhere" to "যিকোনো ঠাই (Jikuno thai)", "Why" to "কিয় (Kiyo)")
        ),
        "what is your all-time favorite meal and how is it traditionally prepared in your home or culture" to TranslationResult(
            assameseText = "আপোনাৰ আটাইতকৈ প্ৰিয় খাদ্য কি, আৰু ইয়াক আপোনাৰ ঘৰত কেনেকৈ পৰম্পৰাগতভাৱে প্ৰস্তুত কৰা হয়?",
            transliteration = "Aaponar aataitkoi priyo khadyo ki, aaru iyak aaponar ghorot kenekoi poromporagotbhabe prostut kora hoi?",
            wordBreakdown = listOf("Favorite meal" to "প্ৰিয় খাদ্য (Priyo khadyo)", "Traditionally" to "পৰম্পৰাগতভাৱে (Poromporagotbhabe)")
        ),
        "how do you think artificial intelligence and modern smartphones will change our daily lives in the next five years" to TranslationResult(
            assameseText = "অহা পাঁচ বছৰত কৃত্রিম বুদ্ধিমত্তা আৰু আধুনিক স্মাৰ্টফোনে আমাৰ দৈনন্দিন জীৱন কেনেকৈ সলনি কৰিব বুলি আপুনি ভাবে?",
            transliteration = "Oha paas bosonot kritrim buddhimotta aaru smart-phone-e aamar doinondin jibon kenekoi xoloni koribo buli aapuni bhabe?",
            wordBreakdown = listOf("AI" to "কৃত্রিম বুদ্ধিমত্তা (Kritrim buddhimotta)", "Smartphones" to "স্মাৰ্টফোন (Smart-phone)", "Change" to "সলনি কৰা (Xoloni kora)")
        ),
        "tell me about a memorable movie or book that left a strong impression on you what was the core message" to TranslationResult(
            assameseText = "আপোনাৰ মনত গভীৰ সাঁচ বহুওৱা এখন স্মৰণীয় চিনেমা বা কিতাপৰ বিষয়ে কওক। ইয়াৰ মূল বাৰ্তা কি আছিল?",
            transliteration = "Aaponar monot gobhir saas bohuwa ekhon smoroniyo cinema ba kitapor bikhoye kouk. Iyar mul baarta ki aasil?",
            wordBreakdown = listOf("Memorable" to "স্মৰণীয় (Smoroniyo)", "Impression" to "সাঁচ/প্ৰভাৱ (Saas)", "Core message" to "মূল বাৰ্তা (Mul baarta)")
        ),
        "walk me through your ideal morning and evening routine how do you stay organized and motivated" to TranslationResult(
            assameseText = "আপোনাৰ এটা আদৰ্শ ৰাতিপুৱা আৰু গধূলিৰ ৰুটিনৰ বিষয়ে কওক। আপুনি কেনেকৈ শৃংখলাবদ্ধ হৈ থাকে?",
            transliteration = "Aaponar eta aadorxo raatipuwa aaru godhulir routinor bikhoye kouk. Aapuni kenekoi sringkholaboddho hoi thake?",
            wordBreakdown = listOf("Routine" to "ৰুটিন (Routine)", "Organized" to "শৃংখলাবদ্ধ (Sringkholaboddho)", "Motivated" to "উৎসাহিত (Utsahito)")
        ),
        "describe the most colorful holiday or festival celebrated in your region how do families come together" to TranslationResult(
            assameseText = "আপোনাৰ অঞ্চলত উদযাপিত আটাইতকৈ ৰঙীন উৎসৱটোৰ বিষয়ে বৰ্ণনা কৰক। পৰিয়ালবোৰ কেনেকৈ একত্ৰিত হয়?",
            transliteration = "Aaponar onsolot ujjyapit aataitkoi rongin utsob-tor bikhoye bornona korok. Poriyalbor kenekoi ekotrito hoi?",
            wordBreakdown = listOf("Festival" to "উৎসৱ (Utsob)", "Region" to "অঞ্চল (Onsol)", "Families" to "পৰিয়ালবোৰ (Poriyalbor)")
        ),
        "what simple everyday habits can people adopt to help protect the environment and keep our cities clean" to TranslationResult(
            assameseText = "পৰিৱেশ সুৰক্ষা আৰু চহৰসমূহ পৰিষ্কাৰ ৰাখিবলৈ মানুহে কি কি সহজ দৈনন্দিন অভ্যাস গঢ়ি তুলিব পাৰে?",
            transliteration = "Poribex xurokkha aaru xohorxomuh poriskar rakhiboloi manuhe ki ki xohoj doinondin obhyax godhi tulibo pare?",
            wordBreakdown = listOf("Protect environment" to "পৰিৱেশ সুৰক্ষা (Poribex xurokkha)", "Clean" to "পৰিষ্কাৰ (Poriskar)")
        ),
        "can you share a time when you faced a difficult challenge and how you managed to overcome it" to TranslationResult(
            assameseText = "আপুনি কেতিয়াবা সম্মুখীন হোৱা এটা কঠিন প্ৰত্যাহ্বান আৰু তাক কেনেকৈ অতিক্ৰম কৰিলে, সেই অভিজ্ঞতা ক'ব পাৰিবনে?",
            transliteration = "Aapuni ketiyaba xommukhin howa eta kothin protyahban aaru taak kenekoi otikrom korile, xei obhiggyota kobo paribone?",
            wordBreakdown = listOf("Challenge" to "প্ৰত্যাহ্বান (Protyahban)", "Overcome" to "অতিক্ৰম কৰা (Otikrom kora)")
        ),
        "how was your weekend did you do anything fun or exciting" to TranslationResult(
            assameseText = "তোমাৰ সপ্তাহান্তৰ ছুটিটো কেনে আছিল? কিবা আমোদজনক কাম কৰিলা নেকি?",
            transliteration = "Tomar xoptahantor sutito kene aasil? Kiba aamodjonok kaam korila neki?",
            wordBreakdown = listOf("Weekend" to "সপ্তাহান্ত (Xoptahanto)", "Fun" to "আমোদজনক (Aamodjonok)", "Exciting" to "উত্তেজনাপূৰ্ণ (Uttejonapurno)")
        ),
        "what is your name" to TranslationResult(
            assameseText = "তোমাৰ নাম কি? / আপোনাৰ নাম কি?",
            transliteration = "Tomar naam ki? / Aaponar naam ki?",
            wordBreakdown = listOf("Name" to "নাম (Naam)", "What" to "কি (Ki)")
        ),
        "where do you live" to TranslationResult(
            assameseText = "তুমি ক'ত থাকা? / আপুনি ক'ত থাকে?",
            transliteration = "Tumi kot thaka? / Aapuni kot thake?",
            wordBreakdown = listOf("Where" to "ক'ত (Kot)", "Live" to "থাকা/থাকে (Thaka/Thake)")
        ),
        "how was your day" to TranslationResult(
            assameseText = "তোমাৰ আজিৰ দিনটো কেনে আছিল?",
            transliteration = "Tomar aazir dinto kene aasil?",
            wordBreakdown = listOf("Day" to "দিন (Din)", "How was" to "কেনে আছিল (Kene aasil)")
        ),
        "tell me about your family" to TranslationResult(
            assameseText = "তোমাৰ পৰিয়ালৰ বিষয়ে কোৱা।",
            transliteration = "Tomar poriyalor bikhoye kowa.",
            wordBreakdown = listOf("Family" to "পৰিয়াল (Poriyal)", "About" to "বিষয়ে (Bikhoye)")
        ),
        "why do you want to learn english" to TranslationResult(
            assameseText = "তুমি ইংৰাজী কিয় শিকিব বিচাৰা?",
            transliteration = "Tumi ingraji kiyo xikibo bisara?",
            wordBreakdown = listOf("Why" to "কিয় (Kiyo)", "Learn" to "শিকা (Xika)", "Want" to "বিচাৰা (Bisara)")
        ),
        "what are your plans for tomorrow" to TranslationResult(
            assameseText = "কাইলৈৰ বাবে তোমাৰ কি পৰিকল্পনা আছে?",
            transliteration = "Kailoir babe tomar ki porikolpona aase?",
            wordBreakdown = listOf("Plans" to "পৰিকল্পনা (Porikolpona)", "Tomorrow" to "কাইলৈ (Kailoi)")
        ),
        "can you tell me about your best friend" to TranslationResult(
            assameseText = "তোমাৰ প্ৰিয় বন্ধুৰ বিষয়ে ক'ব পাৰিবা নে?",
            transliteration = "Tomar priyo bondhur bikhoye kobo paribane?",
            wordBreakdown = listOf("Best friend" to "প্ৰিয় বন্ধু (Priyo bondhu)", "Tell" to "কোৱা (Kowa)")
        ),
        "do you prefer coffee or tea" to TranslationResult(
            assameseText = "তুমি কফি ভাল পোৱা নে চাহ?",
            transliteration = "Tumi coffee bhal powa ne sah?",
            wordBreakdown = listOf("Prefer" to "পছন্দ কৰা (Posondo kora)", "Coffee" to "কফি (Coffee)", "Tea" to "চাহ (Sah)")
        ),
        "what do you like to do in your free time" to TranslationResult(
            assameseText = "আজৰি সময়ত তুমি কি কৰি ভাল পোৱা?",
            transliteration = "Aajori xomoyot tumi ki kori bhal powa?",
            wordBreakdown = listOf("Free time" to "আজৰি সময় (Aajori xomoi)", "Like to do" to "কৰি ভাল পোৱা (Kori bhal powa)")
        ),
        "tell me about your favorite food" to TranslationResult(
            assameseText = "তোমাৰ প্ৰিয় খাদ্যৰ বিষয়ে কোৱা।",
            transliteration = "Tomar priyo khadyor bikhoye kowa.",
            wordBreakdown = listOf("Favorite food" to "প্ৰিয় খাদ্য (Priyo khadyo)", "Tell" to "কোৱা (Kowa)")
        ),
        "what languages can you speak" to TranslationResult(
            assameseText = "তুমি কি কি ভাষা ক'ব পাৰা?",
            transliteration = "Tumi ki ki bhaxa kobo para?",
            wordBreakdown = listOf("Languages" to "ভাষাসমূহ (Bhaxaxomuh)", "Speak" to "কোৱা (Kowa)")
        )
    )

    // Comprehensive English-to-Assamese bilingual dictionary
    private val wordMap = mapOf(
        // Core Pronouns, Articles, Auxiliaries & Prepositions
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
        "me" to Pair("মোক", "Mook"),
        "us" to Pair("আমাক", "Aamak"),
        "them" to Pair("সিহঁতক / সেইবোৰক", "Xeiborok"),
        "him" to Pair("তেওঁক", "Teok"),
        "yourself" to Pair("আপোনাৰ নিজৰ", "Aaponar nijor"),
        "myself" to Pair("মোৰ নিজৰ", "Moor nijor"),
        "am" to Pair("হওঁ / আছোঁ", "Aasu"),
        "is" to Pair("হয় / আছে", "Hoi / Aase"),
        "are" to Pair("হয় / আছে", "Hoi / Aase"),
        "was" to Pair("আছিল", "Aasil"),
        "were" to Pair("আছিল", "Aasil"),
        "be" to Pair("হোৱা", "Howa"),
        "been" to Pair("হৈছে", "Hoise"),
        "have" to Pair("আছে", "Aase"),
        "has" to Pair("আছে", "Aase"),
        "had" to Pair("আছিল", "Aasil"),
        "do" to Pair("কৰা / কৰে", "Kora"),
        "does" to Pair("কৰে", "Kore"),
        "did" to Pair("কৰিলে", "Korile"),
        "can" to Pair("পাৰে", "Pare"),
        "could" to Pair("পাৰিলেহেঁতেন / পাৰিবনে", "Paribone"),
        "will" to Pair("হ'ব / কৰিব", "Hobo"),
        "would" to Pair("হ'ব / বিচাৰিব", "Bisaribo"),
        "should" to Pair("উচিত", "Usit"),
        "a" to Pair("এটা / এজন", "Eta"),
        "an" to Pair("এটা / এজন", "Eta"),
        "the" to Pair("এই / সেই", "Ei"),
        "in" to Pair("ভিতৰত / -ত", "-ot"),
        "on" to Pair("ওপৰত / -ত", "Oporot"),
        "at" to Pair("-ত", "-ot"),
        "to" to Pair("-লৈ", "-loi"),
        "for" to Pair("বাবে", "Babe"),
        "of" to Pair("-ৰ", "-or"),
        "with" to Pair("সৈতে", "Xoite"),
        "from" to Pair("পৰা", "Pora"),
        "about" to Pair("বিষয়ে", "Bikhoye"),
        "by" to Pair("দ্বাৰা", "Dwara"),
        "and" to Pair("আৰু", "Aaru"),
        "or" to Pair("বা", "Ba"),
        "but" to Pair("কিন্তু", "Kintu"),
        "if" to Pair("যদি", "Jodi"),
        "because" to Pair("কাৰণ", "Karon"),
        "so" to Pair("সেয়েহে / গতিকে", "Xeyehe"),
        "not" to Pair("নহয় / ন-", "Nohoi"),
        "yes" to Pair("হয়", "Hoi"),
        "no" to Pair("নহয়", "Nohoi"),
        "very" to Pair("বহুত / অতি", "Bohut"),
        "more" to Pair("অধিক", "Odhik"),
        "most" to Pair("আটাইতকৈ", "Aataitkoi"),
        "also" to Pair("লগতে / ও", "Logote"),
        "usually" to Pair("সাধাৰণতে", "Sadharonote"),
        "want" to Pair("বিচৰা", "Bisora"),
        "need" to Pair("প্ৰয়োজন", "Proyojon"),
        "name" to Pair("নাম", "Naam"),
        "favorite" to Pair("প্ৰিয়", "Priyo"),
        "favourite" to Pair("প্ৰিয়", "Priyo"),
        "hobby" to Pair("চখ", "Sokh"),
        "hobbies" to Pair("চখবোৰ", "Sokhbor"),
        "weekend" to Pair("সপ্তাহান্ত", "Xoptahanto"),
        "weekends" to Pair("সপ্তাহান্তবোৰত", "Xoptahantoborot"),
        "weekdays" to Pair("কৰ্মদিনবোৰত", "Kormodinborot"),
        "fun" to Pair("আমোদজনক", "Aamodjonok"),
        "exciting" to Pair("উত্তেজনাপূৰ্ণ", "Uttejonapurno"),
        "enjoy" to Pair("ভাল পোৱা / উপভোগ কৰা", "Bhal powa"),
        "share" to Pair("ভাগ বতৰা কৰা / কোৱা", "Kowa"),
        "story" to Pair("কাহিনী / গল্প", "Kahini"),
        "describe" to Pair("বৰ্ণনা কৰা", "Bornona kora"),
        "detail" to Pair("সবিশেষ", "Xobixex"),
        "details" to Pair("সবিশেষ তথ্য", "Xobixex"),
        "another" to Pair("আন এটা", "Aan eta"),
        "else" to Pair("আন কিবা", "Aan kiba"),
        "anything" to Pair("কিবা", "Kiba"),
        "something" to Pair("কিবা এটা", "Kiba eta"),
        "topic" to Pair("বিষয়", "Bixoy"),
        "introduction" to Pair("পৰিচয়", "Porisoy"),
        "activity" to Pair("কাৰ্যকলাপ / কাম", "Kaam"),
        "activities" to Pair("কামবোৰ", "Kaambor"),
        "routine" to Pair("দৈনন্দিন ৰুটিন", "Routine"),
        "habit" to Pair("অভ্যাস", "Obhyax"),
        "habits" to Pair("অভ্যাসবোৰ", "Obhyaxbor"),
        "stay" to Pair("থকা", "Thoka"),
        "keep" to Pair("ৰখা", "Rokha"),
        "prefer" to Pair("পছন্দ কৰা", "Posondo kora"),
        "change" to Pair("সলনি কৰা", "Xoloni kora"),
        "thing" to Pair("কথা / বস্তু", "Kotha"),
        "things" to Pair("কথাবোৰ / বস্তুবোৰ", "Kothabor"),
        "travel" to Pair("ভ্ৰমণ কৰা", "Bhromon kora"),
        "career" to Pair("কেৰিয়াৰ / জীৱিকা", "Jibika"),
        "profession" to Pair("বৃত্তি", "Britti"),
        "movie" to Pair("চিনেমা", "Cinema"),
        "meal" to Pair("আহাৰ / খাদ্য", "Khadyo"),
        "festival" to Pair("উৎসৱ", "Utsob"),
        "plan" to Pair("পৰিকল্পনা", "Porikolpona"),
        "plans" to Pair("পৰিকল্পনাসমূহ", "Porikolpona"),
        "goals" to Pair("লক্ষ্যসমূহ", "Lokkhyo"),
        "personal" to Pair("ব্যক্তিগত", "Byoktigoto"),
        "memorable" to Pair("স্মৰণীয়", "Smoroniyo"),
        "moment" to Pair("মুহূৰ্ত", "Muhurto"),
        "advice" to Pair("পৰামৰ্শ", "Poramorkho"),
        "spend" to Pair("খৰচ কৰা / কটোৱা", "Kotowa"),
        "connect" to Pair("সংযোগ হোৱা", "Xongjog howa"),
        "related" to Pair("জড়িত / সম্পৰ্কীয়", "Jorito"),
        "organized" to Pair("শৃংখলাবদ্ধ", "Sringkholaboddho"),
        "motivated" to Pair("উৎসাহিত", "Utsahito"),
        "one" to Pair("এটা", "Eta"),
        "five" to Pair("পাঁচ", "Paas"),

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

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val aiTranslationCache = ConcurrentHashMap<String, TranslationResult>()

    /**
     * Extracts the clean question or core speaking prompt from Nova messages or system wraps.
     * E.g. "Hi! I'm Nova 😊\nLet's practice “Introduction”.\n\n🕒 Hello! How are you?" -> "Hello! How are you?"
     */
    fun extractCleanQuestion(text: String): String {
        if (text.isBlank()) return "Hello! How are you?"
        val trimmed = text.trim()
        val lines = trimmed.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines.reversed()) {
            val stripped = line.replace("🕒", "").replace("💬", "").replace("⏰", "").trim()
            if (stripped.contains("?") || stripped.startsWith("Tell me", ignoreCase = true) ||
                stripped.startsWith("Walk me", ignoreCase = true) || stripped.startsWith("Describe", ignoreCase = true)) {
                return cleanPunctuationAndWrappers(stripped)
            }
        }
        if (trimmed.contains("🕒")) {
            val afterClock = trimmed.substringAfterLast("🕒").trim()
            if (afterClock.isNotBlank()) {
                return cleanPunctuationAndWrappers(afterClock)
            }
        }
        if (trimmed.contains("Nova", ignoreCase = true) && trimmed.contains("practice", ignoreCase = true)) {
            val sentences = trimmed.split(".", "!", "\n").map { it.trim() }.filter { it.isNotBlank() }
            val candidate = sentences.lastOrNull { !it.contains("practice", ignoreCase = true) && !it.contains("Nova", ignoreCase = true) }
            if (!candidate.isNullOrBlank()) {
                return cleanPunctuationAndWrappers(candidate)
            }
        }
        return cleanPunctuationAndWrappers(lines.lastOrNull() ?: trimmed)
    }

    private fun cleanPunctuationAndWrappers(s: String): String {
        return s.replace("🕒", "")
            .replace("💬", "")
            .replace("😊", "")
            .replace("⏰", "")
            .replace("“", "\"")
            .replace("”", "\"")
            .trim()
    }

    private fun cleanWordMeaning(rawMeaning: String): String {
        return rawMeaning.substringBefore("/").trim()
    }

    /**
     * Translates the given English sentence to Assamese.
     * Uses custom Assamese text if provided, otherwise looks up curated sentences,
     * applies smart grammatical patterns, or dynamically constructs a bilingual translation.
     */
    fun translate(englishSentence: String, customAssamese: String? = null): TranslationResult {
        val cleanQuestion = extractCleanQuestion(englishSentence)

        if (!customAssamese.isNullOrBlank()) {
            val breakdown = generateBreakdown(cleanQuestion)
            return TranslationResult(
                assameseText = customAssamese.trim(),
                transliteration = "অসমীয়া অৰ্থ (Assamese Meaning)",
                wordBreakdown = breakdown
            )
        }

        val normalized = cleanQuestion.trim().lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        curatedSentences[normalized]?.let { return it }

        // Try exact compact match, or long-phrase containment only (never match short greetings like "hi" inside words like "this")
        val compactNorm = normalized.replace(" ", "")
        for ((key, value) in curatedSentences) {
            val compKey = key.replace(" ", "")
            if (compKey == compactNorm || (compKey.length >= 20 && compactNorm.contains(compKey))) {
                return value
            }
        }

        // Conversational Pattern Synthesis for English Questions -> Assamese
        val lower = cleanQuestion.lowercase().trim()
        val synthesized = synthesizeQuestionTranslation(cleanQuestion, lower)
        if (synthesized != null) {
            return synthesized
        }

        // Dynamic synthesis based on vocabulary with clean singular meanings (no ugly slashes)
        val tokens = cleanQuestion.split(Regex("\\s+")).filter { it.isNotBlank() }
        val breakdown = mutableListOf<Pair<String, String>>()
        val assameseWords = mutableListOf<String>()
        val transliterationWords = mutableListOf<String>()

        for (token in tokens) {
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            val match = translateWord(clean)
            if (match != null) {
                val cleanMeaning = cleanWordMeaning(match.first)
                assameseWords.add(cleanMeaning)
                transliterationWords.add(match.second)
                breakdown.add(token to "$cleanMeaning (${match.second})")
            } else {
                assameseWords.add(token)
                transliterationWords.add(token)
                breakdown.add(token to token)
            }
        }

        val isQuestion = cleanQuestion.contains("?") || lower.startsWith("what") || lower.startsWith("where") ||
                lower.startsWith("when") || lower.startsWith("why") || lower.startsWith("how") ||
                lower.startsWith("do") || lower.startsWith("can") || lower.startsWith("could")

        val assameseText = if (assameseWords.isNotEmpty()) {
            val joined = assameseWords.joinToString(" ")
            if (isQuestion) "$joined?" else "$joined।"
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

    private fun synthesizeQuestionTranslation(original: String, lower: String): TranslationResult? {
        val breakdown = generateBreakdown(original)
        // If multi-sentence coach reply (e.g. "Good effort! ... What is another detail...?"), examine the question sentence too
        val questionClause = original.split(Regex("(?<=[.!?])\\s+"))
            .lastOrNull { it.contains("?") }
            ?.trim()
            ?.lowercase()
            ?: lower
        val check = if (questionClause.isNotBlank()) questionClause else lower

        return when {
            lower.contains("hello") && lower.contains("how are you") -> TranslationResult(
                assameseText = "নমস্কাৰ! আপুনি কেনে আছে?",
                transliteration = "Nomoxkar! Aapuni kene aase?",
                wordBreakdown = breakdown
            )
            lower.contains("tell me about yourself") || check.contains("tell me about yourself") -> TranslationResult(
                assameseText = "আপোনাৰ বিষয়ে কওক।",
                transliteration = "Aaponar bikhoye kouk.",
                wordBreakdown = breakdown
            )
            lower.startsWith("tell me about") || check.startsWith("tell me about") -> {
                val topic = (if (check.startsWith("tell me about")) check else original)
                    .substringAfter("about", "")
                    .replace("?", "")
                    .replace(".", "")
                    .trim()
                TranslationResult(
                    assameseText = if (topic.isNotBlank()) "আপোনাৰ $topic ৰ বিষয়ে কওক।" else "এই বিষয়ে আপুনি কওক।",
                    transliteration = "Aaponar bikhoye kouk.",
                    wordBreakdown = breakdown
                )
            }
            check.contains("how are you") -> TranslationResult(
                assameseText = "আপুনি কেনে আছে?",
                transliteration = "Aapuni kene aase?",
                wordBreakdown = breakdown
            )
            check.contains("how was your day") -> TranslationResult(
                assameseText = "তোমাৰ আজিৰ দিনটো কেনে আছিল?",
                transliteration = "Tomar aazir dinto kene aasil?",
                wordBreakdown = breakdown
            )
            check.contains("how was your weekend") -> TranslationResult(
                assameseText = "তোমাৰ সপ্তাহান্তৰ ছুটিটো কেনে আছিল?",
                transliteration = "Tomar xoptahantor sutito kene aasil?",
                wordBreakdown = breakdown
            )
            check.contains("what is your name") -> TranslationResult(
                assameseText = "তোমাৰ নাম কি? / আপোনাৰ নাম কি?",
                transliteration = "Tomar naam ki? / Aaponar naam ki?",
                wordBreakdown = breakdown
            )
            check.contains("where do you live") -> TranslationResult(
                assameseText = "তুমি ক'ত থাকা? / আপুনি ক'ত থাকে?",
                transliteration = "Tumi kot thaka? / Aapuni kot thake?",
                wordBreakdown = breakdown
            )
            check.contains("another detail") || check.contains("what else can you tell me") -> TranslationResult(
                assameseText = "এই বিষয়ে আপুনি আন কিবা কথা ক'ব বিচাৰিবনে?",
                transliteration = "Ei bikhoye aapuni aan kiba kotha kobo bisaribone?",
                wordBreakdown = breakdown
            )
            check.contains("another activity or routine") -> TranslationResult(
                assameseText = "এই বিষয়ৰ লগত জড়িত আন কিবা কাম বা দৈনন্দিন অভ্যাস আপুনি ভাল পায়নে?",
                transliteration = "Ei bikhoyor logot jorito aan kiba kaam ba doinondin obhyax aapuni bhal payne?",
                wordBreakdown = breakdown
            )
            check.contains("connect with your weekend or personal goals") -> TranslationResult(
                assameseText = "এই বিষয়টো আপোনাৰ সপ্তাহান্ত বা ব্যক্তিগত লক্ষ্যৰ সৈতে কেনেকৈ জড়িত?",
                transliteration = "Ei bikhoytu aaponar xoptahanto ba byoktigoto lokkhyor xoite kenekoi jorito?",
                wordBreakdown = breakdown
            )
            check.contains("memorable story or experience") || check.contains("memorable moment") -> TranslationResult(
                assameseText = "আপুনি এই বিষয়ৰ লগত জড়িত এটা স্মৰণীয় কাহিনী বা অভিজ্ঞতা ক'ব পাৰিবনে?",
                transliteration = "Aapuni ei bikhoyor logot jorito eta smoroniyo kahini ba obhiggyota kobo paribone?",
                wordBreakdown = breakdown
            )
            check.contains("something challenging") || check.contains("what advice would you give") -> TranslationResult(
                assameseText = "এই ক্ষেত্ৰত কিবা প্ৰত্যাহ্বান আছে নেকি, আৰু আপুনি কি পৰামৰ্শ দিব?",
                transliteration = "Ei khetrot kiba protyahban aase neki, aaru aapuni ki poramorkho dibo?",
                wordBreakdown = breakdown
            )
            check.contains("if you could change one thing") -> TranslationResult(
                assameseText = "যদি আপুনি ইয়াৰ সৈতে জড়িত এটা কথা সলনি কৰিব পাৰিলেহেঁতেন, তেন্তে সেইটো কি হ'লহেঁতেন?",
                transliteration = "Jodi aapuni iyar xoite jorito eta kotha xoloni koribo parileheten, tente xeitu ki holheten?",
                wordBreakdown = breakdown
            )
            check.contains("important to you personally") -> TranslationResult(
                assameseText = "এই বিষয়টো আপোনাৰ বাবে ব্যক্তিগতভাৱে কিয় গুৰুত্বপূৰ্ণ?",
                transliteration = "Ei bikhoytu aaponar babe byoktigotobhabe kiyo gurutwopurno?",
                wordBreakdown = breakdown
            )
            check.contains("what challenges do beginners face") -> TranslationResult(
                assameseText = "নতুনসকলে কি কি অসুবিধাৰ সন্মুখীন হয়, আৰু তেওঁলোকে কেনেকৈ উন্নতি কৰিব পাৰে?",
                transliteration = "Notunxokole ki ki oxubidhar xommukhin hoi, aaru teoloke kenekoi unnoti koribo pare?",
                wordBreakdown = breakdown
            )
            check.contains("future goals or plans") -> TranslationResult(
                assameseText = "এই বিষয়ত ভৱিষ্যতে আপোনাৰ লক্ষ্য বা পৰিকল্পনা কি?",
                transliteration = "Ei bikhoyot bhobisyote aaponar lokkhyo ba porikolpona ki?",
                wordBreakdown = breakdown
            )
            check.startsWith("what is your favorite") || check.startsWith("whats your favorite") -> {
                val item = check.substringAfter("favorite", "").replace("?", "").trim()
                TranslationResult(
                    assameseText = "তোমাৰ আটাইতকৈ প্ৰিয় $item কি?",
                    transliteration = "Tomar aataitkoi priyo $item ki?",
                    wordBreakdown = breakdown
                )
            }
            check.startsWith("can you") || check.startsWith("could you") -> TranslationResult(
                assameseText = "আপুনি এই বিষয়ে ক'ব পাৰিবনে?",
                transliteration = "Aapuni ei bikhoye kobo paribone?",
                wordBreakdown = breakdown
            )
            check.startsWith("why do you") || check.startsWith("why is") -> TranslationResult(
                assameseText = "আপুনি এইটো কিয় কৰে বা ভাবে?",
                transliteration = "Aapuni eitu kiyo kore ba bhabe?",
                wordBreakdown = breakdown
            )
            check.startsWith("when do you") -> TranslationResult(
                assameseText = "আপুনি সাধাৰণতে এইটো কেতিয়া কৰে?",
                transliteration = "Aapuni sadharonote eitu ketiya kore?",
                wordBreakdown = breakdown
            )
            check.startsWith("where would you") || check.startsWith("where do you") -> TranslationResult(
                assameseText = "আপুনি ক'লৈ যাব বিচাৰে বা ক'ত থাকে?",
                transliteration = "Aapuni koloi jaabo bisare ba kot thake?",
                wordBreakdown = breakdown
            )
            else -> null
        }
    }

    /**
     * Translates in real-time using Google Gemini if API key is provided,
     * falling back to the rich curated Assamese translation engine.
     */
    suspend fun translateWithAi(
        englishSentence: String,
        apiKey: String,
        model: String = "gemini-3.5-flash-lite"
    ): TranslationResult = withContext(Dispatchers.IO) {
        val clean = extractCleanQuestion(englishSentence)
        val normalized = clean.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

        aiTranslationCache[normalized]?.let { return@withContext it }
        curatedSentences[normalized]?.let { return@withContext it }

        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank() || trimmedKey == "MY_GEMINI_API_KEY" || trimmedKey == "your_api_key_here") {
            return@withContext translate(clean)
        }

        try {
            val cleanModel = model.trim().ifEmpty { "gemini-3.5-flash-lite" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$trimmedKey"
            val prompt = """
                Translate this English sentence/question into natural, polite, colloquial Assamese (অসমীয়া).
                Also provide natural Latin transliteration (pronunciation guide in English alphabet) and a brief word-by-word breakdown.
                
                English: "$clean"
                
                Respond ONLY with valid JSON using this format:
                {
                  "assameseText": "...",
                  "transliteration": "...",
                  "wordBreakdown": [
                    {"english": "Word", "meaning": "অৰ্থ (Pronunciation)"}
                  ]
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val partObj = JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    }
                    put(partObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val root = JSONObject(respBody)
                val rawTextCandidate = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                val cleanedJson = rawTextCandidate
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val parsed = JSONObject(cleanedJson)
                val asmText = parsed.optString("assameseText").ifBlank { null }
                val trans = parsed.optString("transliteration", "")
                val breakdownArr = parsed.optJSONArray("wordBreakdown")
                val breakdownList = mutableListOf<Pair<String, String>>()
                if (breakdownArr != null) {
                    for (i in 0 until breakdownArr.length()) {
                        val item = breakdownArr.optJSONObject(i)
                        if (item != null) {
                            val eng = item.optString("english")
                            val m = item.optString("meaning")
                            if (eng.isNotBlank()) breakdownList.add(eng to m)
                        }
                    }
                }

                if (!asmText.isNullOrBlank()) {
                    val result = TranslationResult(
                        assameseText = asmText,
                        transliteration = trans,
                        wordBreakdown = if (breakdownList.isNotEmpty()) breakdownList else generateBreakdown(clean)
                    )
                    aiTranslationCache[normalized] = result
                    return@withContext result
                }
            }
        } catch (_: Throwable) {}

        val fallback = translate(clean)
        aiTranslationCache[normalized] = fallback
        return@withContext fallback
    }

    private fun generateBreakdown(englishSentence: String): List<Pair<String, String>> {
        val tokens = englishSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        val breakdown = mutableListOf<Pair<String, String>>()
        for (token in tokens) {
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            val match = translateWord(clean)
            if (match != null) {
                val cleanMeaning = cleanWordMeaning(match.first)
                breakdown.add(token to "$cleanMeaning (${match.second})")
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
