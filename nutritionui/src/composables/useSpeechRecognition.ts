import { ref, onUnmounted } from 'vue'

export function useSpeechRecognition() {
  const isListening = ref(false)
  const transcript = ref('')
  const interimTranscript = ref('')
  const error = ref<string | null>(null)
  const isSupported = ref(false)

  let recognition: any = null

  // Cross-browser SpeechRecognition
  const SpeechRecognitionAPI =
    (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

  if (SpeechRecognitionAPI) {
    isSupported.value = true
    recognition = new SpeechRecognitionAPI()
    recognition.continuous = false
    recognition.interimResults = true
    recognition.lang = 'zh-CN'
    recognition.maxAlternatives = 1

    recognition.onresult = (event: any) => {
      let interim = ''
      let final = ''
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        if (result.isFinal) {
          final += result[0].transcript
        } else {
          interim += result[0].transcript
        }
      }
      if (final) transcript.value += final
      interimTranscript.value = interim
    }

    recognition.onerror = (event: any) => {
      error.value = event.error === 'no-speech' ? '未检测到语音，请重试' : event.error
      isListening.value = false
    }

    recognition.onend = () => {
      isListening.value = false
      interimTranscript.value = ''
    }
  }

  const start = () => {
    if (!recognition) return
    error.value = null
    transcript.value = ''
    interimTranscript.value = ''
    try {
      recognition.start()
      isListening.value = true
    } catch (e) {
      // Already started
    }
  }

  const stop = () => {
    if (recognition) {
      try {
        recognition.stop()
      } catch (e) {
        // Already stopped
      }
    }
    isListening.value = false
  }

  const reset = () => {
    transcript.value = ''
    interimTranscript.value = ''
    error.value = null
  }

  onUnmounted(() => {
    if (recognition) {
      try {
        recognition.abort()
      } catch (e) {
        // Ignore
      }
    }
  })

  return { isListening, transcript, interimTranscript, error, isSupported, start, stop, reset }
}
