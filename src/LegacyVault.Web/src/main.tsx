import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { store } from './store';
import App from './App';
import './index.css';

// Apply saved theme + font size before first render to avoid flash
const savedTheme = localStorage.getItem('lv_theme');
document.documentElement.setAttribute('data-theme', savedTheme === 'light' ? 'light' : 'dark');

const savedFontSize = localStorage.getItem('lv_fontsize');
document.documentElement.setAttribute(
  'data-fontsize',
  ['small', 'medium', 'large'].includes(savedFontSize!) ? savedFontSize! : 'medium'
);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </StrictMode>
);
