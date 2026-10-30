package com.example.model

enum class TriggerCategory {
    TIREDNESS,
    ANXIETY,
    SADNESS,
    ANGER,
    SHAME,
    HEAVINESS,
    NEUTRAL
}

object TriggerSystem {

    // Exact lists provided by the user + Kyrgyz and English equivalents
    private val shameTriggers = listOf(
        // Russian exact list
        "стыдно", "стыд", "виноват", "виновата", "я плохой", "я плохая",
        "я ужасный", "я ужасная", "ненавижу себя", "я ничтожество", "ничтожество",
        // Kyrgyz
        "уялам", "уят", "күнөөлүүмүн", "күнөөлүү", "күнөө", "мен жаманмын",
        "өзүмдү жек көрөм", "мен эч ким эмесмин", "өзүмдү күнөөлөйм",
        // English
        "ashamed", "shame", "guilty", "guilt", "my fault", "i am bad",
        "i am terrible", "hate myself", "i am worthless", "worthless"
    )

    private val heavinessTriggers = listOf(
        // Russian exact list
        "тяжело", "очень тяжело", "не могу", "невозможно", "нет выхода",
        "всё плохо", "все плохо", "всё рушится", "все рушится", "не справлюсь",
        "нет смысла", "не могу больше", "выхода нет", "сил больше нет",
        // Kyrgyz
        "оор", "оор болуп жатат", "абдан оор", "колумдан келбейт", "мүмкүн эмес",
        "айла жок", "баары кыйрады", "көтөрө албайм", "мааниси жок", "чыдай албайм",
        // English
        "heavy", "very heavy", "hard", "can't take it", "cant take it", "impossible",
        "no way out", "falling apart", "i can't do this", "i cant do this", "no point", "overwhelmed"
    )

    private val tirednessTriggers = listOf(
        // Russian exact list
        "устал", "устала", "устали", "усталость", "нет сил", "нет силы", "сил нет",
        "выгорел", "выгорела", "выгорели", "выгорание", "измотан", "измотана",
        "обессилел", "обессилела", "нет энергии", "нету сил", "нету энергии",
        // Kyrgyz
        "чарчадым", "чарчап", "чарчатуу", "чарчоо", "алым жок", "күчүм жок",
        "күч калбады", "чарчап бүттүм", "күчүм калган жок", "күч жок", "энергия жок",
        // English
        "tired", "exhausted", "burnout", "burned out", "no energy", "drained",
        "weary", "no strength", "out of energy", "fatigue"
    )

    private val anxietyTriggers = listOf(
        // Russian exact list
        "тревога", "тревожно", "боюсь", "страх", "страшно", "паника",
        "паникую", "беспокоюсь", "волнуюсь", "нервничаю", "страшно очень",
        "очень страшно", "паническая", "трясет", "дрожу",
        // Kyrgyz
        "корком", "коркунуч", "коркунучтуу", "тынчсыздануу", "тынчсызданам",
        "паника", "кабатырланам", "коркуп жатам", "корком абдан", "тынчсызданып",
        // English
        "anxiety", "anxious", "fear", "scared", "afraid", "panic",
        "panicking", "worry", "worried", "nervous", "very scared"
    )

    private val sadnessTriggers = listOf(
        // Russian exact list
        "грустно", "грусть", "тоска", "тоскливо", "пусто", "пустота",
        "одиноко", "одинок", "одна", "один", "никто не нужен", "никому не нужен",
        "никому не нужна", "мне плохо", "очень плохо", "плачу", "слезы",
        // Kyrgyz
        "капамын", "капа", "кайгы", "муң", "боштук", "жалгыз",
        "жалгыздык", "эч кимге керегим жок", "эч ким жок", "мага жаман", "ыйлап",
        // English
        "sad", "sadness", "sorrow", "empty", "emptiness", "lonely",
        "alone", "nobody cares", "nobody needs me", "i feel bad", "crying"
    )

    private val angerTriggers = listOf(
        // Russian exact list
        "злюсь", "злость", "бесит", "раздражает", "ненавижу", "ярость",
        "злой", "злая", "бесит всё", "бесит все", "взбешен", "взбешена",
        // Kyrgyz
        "ачуум келет", "ачуу", "кыжырым келет", "жиним келет", "жинденип",
        "жек көрөм", "каар", "баары кыжырлантат",
        // English
        "angry", "anger", "mad", "furious", "hate", "irritated",
        "pissed off", "rage", "everything annoys me"
    )

    fun normalizeText(raw: String): String {
        return raw.lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^a-zа-яөүң0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun detectCategory(rawText: String?): TriggerCategory {
        if (rawText.isNullOrBlank()) return TriggerCategory.NEUTRAL
        val normalized = normalizeText(rawText)
        if (normalized.isEmpty()) return TriggerCategory.NEUTRAL

        val padded = " $normalized "
        val words = normalized.split(" ").filter { it.isNotBlank() }

        fun matchesTriggerList(triggers: List<String>): Boolean {
            for (trigger in triggers) {
                val cleanTrigger = normalizeText(trigger)
                if (cleanTrigger.contains(" ")) {
                    // Multi-word phrase: match anywhere in text
                    if (padded.contains(" $cleanTrigger ") || normalized.contains(cleanTrigger)) {
                        return true
                    }
                } else {
                    // Single word: check exact match or root prefix in token list
                    if (words.any { word ->
                            word == cleanTrigger ||
                            (cleanTrigger.length >= 4 && word.startsWith(cleanTrigger))
                        }
                    ) {
                        return true
                    }
                }
            }
            return false
        }

        return when {
            matchesTriggerList(shameTriggers) -> TriggerCategory.SHAME
            matchesTriggerList(heavinessTriggers) -> TriggerCategory.HEAVINESS
            matchesTriggerList(tirednessTriggers) -> TriggerCategory.TIREDNESS
            matchesTriggerList(anxietyTriggers) -> TriggerCategory.ANXIETY
            matchesTriggerList(sadnessTriggers) -> TriggerCategory.SADNESS
            matchesTriggerList(angerTriggers) -> TriggerCategory.ANGER
            else -> TriggerCategory.NEUTRAL
        }
    }

    fun getSupportMessage(category: TriggerCategory, language: AppLanguage): String {
        return when (language) {
            AppLanguage.RUSSIAN -> when (category) {
                TriggerCategory.TIREDNESS -> "«Ты имеешь право устать. Это нормально — можно просто выдохнуть.»"
                TriggerCategory.ANXIETY -> "«Ты не обязан справляться со всем сразу. Сейчас можно просто выдохнуть.»"
                TriggerCategory.SADNESS -> "«Твои чувства важны. Ты не один в этом.»"
                TriggerCategory.ANGER -> "«Злость тоже имеет право быть. Ты можешь её отпустить.»"
                TriggerCategory.SHAME -> "«Ты заслуживаешь мягкости к себе. Ты не обязан быть идеальным.»"
                TriggerCategory.HEAVINESS -> "«Сейчас очень тяжело. Можно не решать всё сразу — просто отпустить эту мысль.»"
                TriggerCategory.NEUTRAL -> "«Мысль отпущена. Можно выдохнуть.»"
            }
            AppLanguage.KYRGYZ -> when (category) {
                TriggerCategory.TIREDNESS -> "«Сен чарчаганга толук акың бар. Бул кадыресе көрүнүш — жөн гана дем чыгарып кой.»"
                TriggerCategory.ANXIETY -> "«Сен бардык нерсени бир эле учурда чечүүгө милдеттүү эмессиң. Азыр жөн гана дем чыгарып кой.»"
                TriggerCategory.SADNESS -> "«Сенин сезимдериң маанилүү. Сен бул жагдайда жалгыз эмессиң.»"
                TriggerCategory.ANGER -> "«Ачууланууга да акың бар. Аны ичиңден чыгарып, коё берсең болот.»"
                TriggerCategory.SHAME -> "«Сен өзүңө жылуу мамиле кылууга татыктуусуң. Кынтыксыз болууга милдеттүү эмессиң.»"
                TriggerCategory.HEAVINESS -> "«Азыр абдан оор болуп турат. Баарын дароо чечпей койсо да болот — жөн гана бул ойду бошот.»"
                TriggerCategory.NEUTRAL -> "«Ой коё берилди. Терең дем алсаң болот.»"
            }
            AppLanguage.ENGLISH -> when (category) {
                TriggerCategory.TIREDNESS -> "“You have the right to feel tired. It is okay — you can simply exhale.”"
                TriggerCategory.ANXIETY -> "“You don't have to handle everything at once. Right now, you can just exhale.”"
                TriggerCategory.SADNESS -> "“Your feelings are valid. You are not alone in this.”"
                TriggerCategory.ANGER -> "“Anger has a right to be felt, too. You can let it go.”"
                TriggerCategory.SHAME -> "“You deserve gentleness towards yourself. You don't have to be perfect.”"
                TriggerCategory.HEAVINESS -> "“It feels very heavy right now. You don't have to solve everything today — just let this thought go.”"
                TriggerCategory.NEUTRAL -> "“Thought released. You can exhale now.”"
            }
        }
    }
}
