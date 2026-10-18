# E-DEN — Premium AI Assistant

E-DEN is a native Android application built with Kotlin and Jetpack Compose (Material 3), powered by the Google Gemini API. It features a reactive, glowing 3D-style celestial orb, voice interactions, streaming responses, persistent conversation history, and rich planet theme customizations.

---

## ✨ Features

- **Celestial Planet Orb**: Interactive 3D-style planet with layered radial gradients, outer glow corona, animated planetary rings, and starlight particles.
- **Dynamic Reactive States**: The orb smoothly transitions between `Idle` (calm pulse), `Listening` (expanding soundwave ripples), `Thinking` (swirling cosmic spin), and `Speaking` (vibrant waveform oscillation).
- **Gemini Streaming Chat**: Real-time markdown rendering (code blocks, bold, headers, lists), copy text, clear chat, and local persistence backed by Android Room Database.
- **Voice Input & Text-to-Speech**: Speech-to-text voice recognition and built-in text-to-speech output.
- **Orb Customization**: Choose from 8 celestial styles (*Saturn, Earth, Mars, Jupiter, Neptune, Nebula, Sun, Moon*), custom color pickers, sliders for size, glow intensity, rotation speed, and planetary ring toggles.
- **API Key Management**: Secure storage via Jetpack DataStore, model switcher (`gemini-2.5-flash`, `gemini-2.5-pro`, `gemini-1.5-flash`, etc.), and a live "Test Key" latency & connectivity diagnostic tool.

---

## 🚀 How to Push to GitHub & Download the APK

### 1. Initialize and Push to GitHub

From your local machine or terminal:

```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit of E-DEN Assistant"

# Create a new repository on GitHub (e.g., https://github.com/your-username/eden-assistant)
git remote add origin https://github.com/your-username/eden-assistant.git
git branch -M main
git push -u origin main
```

### 2. Automatic Build via GitHub Actions

Once pushed, GitHub automatically triggers the CI workflow located in `.github/workflows/android.yml`:
1. Navigates to the **Actions** tab in your GitHub repository.
2. Click on the latest workflow run: **"Android CI Build"**.
3. Under the **Artifacts** section at the bottom of the summary page, download **`apk-debug.apk`**.
4. Transfer and install the APK on your Android device (ensure "Install from unknown sources" is allowed).

---

## 🔑 Setting Up Your Gemini API Key

1. Open E-DEN and go to the **Settings** tab.
2. Obtain a free API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
3. Paste your API key into the input field and tap **Test Key** to verify connectivity.
4. Save your key and start chatting with E-DEN!
