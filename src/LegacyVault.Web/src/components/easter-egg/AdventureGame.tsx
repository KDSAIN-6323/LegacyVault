import React, { useState, useEffect, useRef, useCallback } from 'react';
import { createInitialState, OutputLine } from './gameData';
import { processCommand } from './gameEngine';
import './AdventureGame.css';

// ─── Intro splash ─────────────────────────────────────────────────────────────

const INTRO: OutputLine[] = [
  { text: '╔══════════════════════════════════════════════════════╗', type: 'heading' },
  { text: '║         LEGACY VAULT ADVENTURE  v1.0                ║', type: 'heading' },
  { text: '║       An Interactive Fiction Experience             ║', type: 'heading' },
  { text: '╚══════════════════════════════════════════════════════╝', type: 'heading' },
  { text: '', type: 'blank' },
  { text: 'Copyright (C) Legacy Vault Systems. All rights reserved.' },
  { text: 'Distributed solely for educational purposes and easter-egg hunting.' },
  { text: '', type: 'blank' },
  { text: 'Type HELP for commands. Type QUIT to close. Press ESC at any time.' },
  { text: '', type: 'blank' },
  { text: '────────────────────────────────────────────────────────' },
  { text: '', type: 'blank' },
];

// ─── Component ────────────────────────────────────────────────────────────────

interface Props {
  onClose: () => void;
}

export const AdventureGame: React.FC<Props> = ({ onClose }) => {
  // Game state lives in a ref — no need to drive re-renders from it.
  const gameRef = useRef(createInitialState());

  // Display history is React state so the terminal re-renders on new output.
  const [history, setHistory] = useState<OutputLine[]>(() => {
    const initial = createInitialState();
    const { state, lines } = processCommand(initial, 'look');
    gameRef.current = state;
    return [...INTRO, ...lines];
  });

  const [input, setInput] = useState('');
  const lastCmdRef = useRef('');
  const cmdHistoryRef = useRef<string[]>([]);
  const historyIdxRef = useRef(-1);

  const inputRef = useRef<HTMLInputElement>(null);
  const outputRef = useRef<HTMLDivElement>(null);

  // Auto-scroll on new output
  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight;
    }
  }, [history]);

  // Focus input on mount
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // ESC to close
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const submit = useCallback(() => {
    const raw = input.trim();
    if (!raw) return;

    // 'again' / 'g' repeats the last command
    const cmd = raw === 'again' || raw === 'g' ? lastCmdRef.current || raw : raw;

    const cmdLine: OutputLine = { text: `> ${raw}`, type: 'cmd' };
    const { state: newState, lines } = processCommand(gameRef.current, cmd);

    gameRef.current = newState;
    lastCmdRef.current = cmd;
    cmdHistoryRef.current = [...cmdHistoryRef.current, raw];
    historyIdxRef.current = -1;

    setHistory(prev => [...prev, { text: '', type: 'blank' }, cmdLine, ...lines]);
    setInput('');

    // Auto-close after quit with a short delay so the farewell message is visible
    if (newState.won && ['quit', 'exit', 'q', 'bye'].includes(cmd.toLowerCase())) {
      setTimeout(onClose, 900);
    }
  }, [input, onClose]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      submit();
      return;
    }

    // Command history navigation
    const hist = cmdHistoryRef.current;
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      const newIdx = Math.min(historyIdxRef.current + 1, hist.length - 1);
      historyIdxRef.current = newIdx;
      setInput(hist[hist.length - 1 - newIdx] ?? '');
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      const newIdx = Math.max(historyIdxRef.current - 1, -1);
      historyIdxRef.current = newIdx;
      setInput(newIdx === -1 ? '' : (hist[hist.length - 1 - newIdx] ?? ''));
    }
  };

  const lineClass = (type?: string): string => {
    switch (type) {
      case 'heading': return 'adv-line--heading';
      case 'win':     return 'adv-line--win';
      case 'error':   return 'adv-line--error';
      case 'dim':     return 'adv-line--dim';
      case 'cmd':     return 'adv-line--cmd';
      default:        return '';
    }
  };

  // Clicking the backdrop closes the game
  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div className="adv-overlay" onClick={handleOverlayClick}>
      <div className="adv-terminal">
        {/* Title bar */}
        <div className="adv-titlebar">
          <span>LEGACY VAULT ADVENTURE v1.0</span>
          <button className="adv-close" onClick={onClose} aria-label="Close game">
            [ CLOSE ]
          </button>
        </div>

        {/* Output */}
        <div className="adv-output" ref={outputRef}>
          {history.map((line, i) =>
            line.type === 'blank' ? (
              <div key={i} className="adv-blank" />
            ) : (
              <p key={i} className={`adv-line ${lineClass(line.type)}`}>
                {line.text}
              </p>
            ),
          )}
        </div>

        {/* Input row */}
        <div className="adv-input-row">
          <span className="adv-prompt">&gt;</span>
          <input
            ref={inputRef}
            className="adv-input"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="What do you do?"
            autoComplete="off"
            autoCorrect="off"
            autoCapitalize="off"
            spellCheck={false}
          />
        </div>
      </div>
    </div>
  );
};

export default AdventureGame;
