package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppLanguage
import com.example.model.TranslationProvider
import com.example.model.TriggerCategory
import com.example.model.TriggerSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SayIt", appName)
    }

    @Test
    fun `verify disclaimer text and button`() {
        val ruStrings = TranslationProvider.get(AppLanguage.RUSSIAN)
        val kyStrings = TranslationProvider.get(AppLanguage.KYRGYZ)
        val enStrings = TranslationProvider.get(AppLanguage.ENGLISH)

        assertEquals("Понятно, начинаем", ruStrings.disclaimerButton)
        assertEquals("Түшүнүктүү, баштайбыз", kyStrings.disclaimerButton)
        assertEquals("Understood, let's start", enStrings.disclaimerButton)

        assertTrue(ruStrings.disclaimerBody.contains("SayIt не является медицинской"))
        assertTrue(ruStrings.disclaimerBody.contains("полностью исчезает после нажатия «Отпустить»"))
    }

    @Test
    fun `verify tiredness trigger words`() {
        val words = listOf(
            "устал", "устала", "устали", "усталость", "нет сил", "нет силы",
            "выгорел", "выгорела", "выгорели", "выгорание", "измотан", "измотана",
            "обессилел", "обессилела", "нет энергии", "сил нет", "УСТАЛ", "НЕТ СИЛ"
        )
        for (w in words) {
            assertEquals("Word '$w' should match TIREDNESS", TriggerCategory.TIREDNESS, TriggerSystem.detectCategory("Я сегодня $w"))
        }
    }

    @Test
    fun `verify anxiety and fear trigger words`() {
        val words = listOf(
            "тревога", "тревожно", "боюсь", "страх", "страшно", "паника",
            "паникую", "беспокоюсь", "волнуюсь", "нервничаю", "страшно очень", "ПАНИКА"
        )
        for (w in words) {
            assertEquals("Word '$w' should match ANXIETY", TriggerCategory.ANXIETY, TriggerSystem.detectCategory("Мне сейчас $w"))
        }
    }

    @Test
    fun `verify sadness and loneliness trigger words`() {
        val words = listOf(
            "грустно", "грусть", "тоска", "тоскливо", "пусто", "пустота",
            "одиноко", "одинок", "одна", "никто не нужен", "никому не нужен",
            "никому не нужна", "мне плохо"
        )
        for (w in words) {
            assertEquals("Word '$w' should match SADNESS", TriggerCategory.SADNESS, TriggerSystem.detectCategory("Сегодня $w"))
        }
    }

    @Test
    fun `verify anger trigger words`() {
        val words = listOf(
            "злюсь", "злость", "бесит", "раздражает", "ненавижу", "ярость",
            "злой", "злая", "бесит всё", "БЕСИТ"
        )
        for (w in words) {
            assertEquals("Word '$w' should match ANGER", TriggerCategory.ANGER, TriggerSystem.detectCategory("Я $w"))
        }
    }

    @Test
    fun `verify shame and guilt trigger words`() {
        val words = listOf(
            "стыдно", "стыд", "виноват", "виновата", "я плохой", "я плохая",
            "я ужасный", "я ужасная", "ненавижу себя", "я ничтожество"
        )
        for (w in words) {
            assertEquals("Word '$w' should match SHAME", TriggerCategory.SHAME, TriggerSystem.detectCategory("Думаю, что $w"))
        }
    }

    @Test
    fun `verify heaviness and hopelessness trigger words`() {
        val words = listOf(
            "тяжело", "очень тяжело", "не могу", "невозможно", "нет выхода",
            "всё плохо", "всё рушится", "не справлюсь", "нет смысла"
        )
        for (w in words) {
            assertEquals("Word '$w' should match HEAVINESS", TriggerCategory.HEAVINESS, TriggerSystem.detectCategory("Мне $w"))
        }
    }

    @Test
    fun `verify default when no triggers matched`() {
        assertEquals(TriggerCategory.NEUTRAL, TriggerSystem.detectCategory("сегодня просто четверг"))
        assertEquals(TriggerCategory.NEUTRAL, TriggerSystem.detectCategory(""))
        assertEquals(TriggerCategory.NEUTRAL, TriggerSystem.detectCategory(null))
    }

    @Test
    fun `verify exact supportive messages`() {
        assertEquals(
            "«Ты имеешь право устать. Это нормально — можно просто выдохнуть.»",
            TriggerSystem.getSupportMessage(TriggerCategory.TIREDNESS, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Ты не обязан справляться со всем сразу. Сейчас можно просто выдохнуть.»",
            TriggerSystem.getSupportMessage(TriggerCategory.ANXIETY, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Твои чувства важны. Ты не один в этом.»",
            TriggerSystem.getSupportMessage(TriggerCategory.SADNESS, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Злость тоже имеет право быть. Ты можешь её отпустить.»",
            TriggerSystem.getSupportMessage(TriggerCategory.ANGER, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Ты заслуживаешь мягкости к себе. Ты не обязан быть идеальным.»",
            TriggerSystem.getSupportMessage(TriggerCategory.SHAME, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Сейчас очень тяжело. Можно не решать всё сразу — просто отпустить эту мысль.»",
            TriggerSystem.getSupportMessage(TriggerCategory.HEAVINESS, AppLanguage.RUSSIAN)
        )
        assertEquals(
            "«Мысль отпущена. Можно выдохнуть.»",
            TriggerSystem.getSupportMessage(TriggerCategory.NEUTRAL, AppLanguage.RUSSIAN)
        )
    }
}
