<template>
  <div class="page">
    <header class="hero">
      <p class="eyebrow">Live completion</p>
      <h1>Prediction demo</h1>
      <p class="subhead">Pause typing to fetch a prediction, then press TAB to accept it.</p>
    </header>

    <section class="card">
      <label class="label" for="input">Your text</label>

      <div class="input-stack">
        <pre class="ghost" aria-hidden="true">
<span class="ghost-user">{{ userText || ' ' }}</span><span v-if="prediction" class="ghost-prediction">{{ prediction }}</span></pre>
        <textarea
          id="input"
          ref="inputRef"
          v-model="userText"
          spellcheck="false"
          rows="6"
          placeholder="I would like to buy a..."
          @input="handleInput"
          @keydown.tab.prevent="applyPrediction"
        ></textarea>
      </div>

      <div class="status">
        <div class="hint">{{ isLoading ? 'Predicting…' : 'Stop typing to get a prediction' }}</div>
        <div class="prediction">
          <span class="pill">TAB</span>
          <span class="prediction-text">
            {{ prediction ? 'Insert prediction' : 'Awaiting prediction' }}
          </span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref } from 'vue';

const userText = ref('');
const prediction = ref('');
const isLoading = ref(false);
const inputRef = ref<HTMLTextAreaElement | null>(null);

let debounceHandle: ReturnType<typeof setTimeout> | null = null;

const handleInput = () => {
  prediction.value = '';
  if (debounceHandle) clearTimeout(debounceHandle);
  debounceHandle = setTimeout(() => {
    requestPrediction();
  }, 600);
};

const requestPrediction = async () => {
  if (!userText.value.trim()) {
    prediction.value = '';
    return;
  }
  isLoading.value = true;
  try {
    const response = await fetch('http://localhost:8080/api/chat/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text: userText.value })
    });

    if (!response.ok) {
      throw new Error(`Request failed: ${response.status}`);
    }

    const data = await response.json();
    prediction.value = data?.prediction ?? '';
  } catch (error) {
    console.error(error);
    prediction.value = '';
  } finally {
    isLoading.value = false;
  }
};

const applyPrediction = async () => {
  if (!prediction.value) return;

  const el = inputRef.value;
  if (!el) return;

  const start = el.selectionStart ?? userText.value.length;
  const end = el.selectionEnd ?? userText.value.length;
  const insert = prediction.value;

  userText.value = userText.value.slice(0, start) + insert + userText.value.slice(end);
  await nextTick();
  const cursor = start + insert.length;
  el.setSelectionRange(cursor, cursor);
  prediction.value = '';
};

onBeforeUnmount(() => {
  if (debounceHandle) {
    clearTimeout(debounceHandle);
  }
});
</script>

<style scoped>
:global(body) {
  margin: 0;
  background: radial-gradient(circle at 20% 20%, #eef2ff 0, #f8fafc 50%, #ffffff 100%);
  font-family: 'Space Grotesk', 'Helvetica Neue', Arial, sans-serif;
  color: #0f172a;
}

.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 48px 20px;
}

.hero {
  text-align: center;
  margin-bottom: 24px;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: #475569;
  margin: 0;
}

h1 {
  margin: 6px 0 8px;
  font-size: 32px;
  font-weight: 700;
}

.subhead {
  margin: 0;
  color: #475569;
}

.card {
  width: min(760px, 100%);
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.label {
  font-weight: 600;
  color: #0f172a;
}

.input-stack {
  position: relative;
}

.ghost {
  position: absolute;
  inset: 0;
  margin: 0;
  padding: 14px 16px;
  font-size: 16px;
  line-height: 1.6;
  font-family: inherit;
  white-space: pre-wrap;
  word-break: break-word;
  color: transparent;
  pointer-events: none;
}

.ghost-user {
  color: #0f172a;
}

.ghost-prediction {
  color: #cbd5e1;
}

textarea {
  width: 100%;
  min-height: 220px;
  border: 1px solid #d7deea;
  border-radius: 14px;
  padding: 14px 16px;
  font-size: 16px;
  line-height: 1.6;
  color: transparent;
  caret-color: #0f172a;
  background: #f8fafc;
  resize: vertical;
  transition: border 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.16);
  background: #ffffff;
}

textarea::placeholder {
  color: #94a3b8;
}

.status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.hint {
  font-size: 14px;
  color: #475569;
}

.prediction {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #94a3b8;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 12px;
  padding: 10px 12px;
}

.prediction-text {
  color: #9ca3af;
}

.prediction.muted .prediction-text {
  color: #cbd5e1;
}

.pill {
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #0f172a;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #d7deea;
  background: #ffffff;
}
</style>
