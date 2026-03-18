import React, { useRef, useState } from 'react';
import type { PasswordContent } from '../../types';
import './PasswordEditor.css';

interface Props {
  content: PasswordContent;
  onChange: (content: PasswordContent) => void;
}

const CHARSET_LOWER = 'abcdefghijklmnopqrstuvwxyz';
const CHARSET_UPPER = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
const CHARSET_DIGITS = '0123456789';
const CHARSET_SYMBOLS = '!@#$%^&*()-_=+[]{}|;:,.<>?';

function generatePassword(length = 20): string {
  const charset = CHARSET_LOWER + CHARSET_UPPER + CHARSET_DIGITS + CHARSET_SYMBOLS;
  // Rejection sampling eliminates modulo bias: discard bytes >= the largest
  // multiple of charset.length that fits in 0-255.
  const limit = 256 - (256 % charset.length);
  const result: string[] = [];
  while (result.length < length) {
    const bytes = crypto.getRandomValues(new Uint8Array((length - result.length) * 2));
    for (const b of bytes) {
      if (result.length >= length) break;
      if (b < limit) result.push(charset[b % charset.length]);
    }
  }
  return result.join('');
}

async function copyToClipboard(text: string) {
  await navigator.clipboard.writeText(text);
}

const PasswordEditor: React.FC<Props> = ({ content, onChange }) => {
  const update = (patch: Partial<PasswordContent>) => onChange({ ...content, ...patch });
  const [showPassword, setShowPassword] = useState(false);
  const [copied, setCopied] = useState<'username' | 'password' | null>(null);
  const copyTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleCopy = async (field: 'username' | 'password') => {
    await copyToClipboard(field === 'username' ? content.username : content.password);
    if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current);
    setCopied(field);
    copyTimeoutRef.current = setTimeout(() => setCopied(null), 1500);
  };

  const handleGenerate = () => {
    update({ password: generatePassword() });
    setShowPassword(true);
  };

  const passwordStrength = getPasswordStrength(content.password);

  return (
    <div className="password-editor">
      {/* URL */}
      <div className="pw-field">
        <label>Website / URL</label>
        <div className="pw-input-row">
          <input
            value={content.url}
            placeholder="https://example.com"
            onChange={(e) => update({ url: e.target.value })}
          />
          {content.url && (
            <a
              href={content.url.startsWith('http') ? content.url : `https://${content.url}`}
              target="_blank"
              rel="noopener noreferrer"
              className="pw-action-btn"
              title="Visit site"
            >
              ↗
            </a>
          )}
        </div>
      </div>

      {/* Username */}
      <div className="pw-field">
        <label>Username / Email</label>
        <div className="pw-input-row">
          <input
            value={content.username}
            placeholder="username@example.com"
            onChange={(e) => update({ username: e.target.value })}
            autoComplete="off"
          />
          <button
            className={`pw-action-btn ${copied === 'username' ? 'pw-action-btn--copied' : ''}`}
            onClick={() => handleCopy('username')}
            title="Copy username"
            disabled={!content.username}
          >
            {copied === 'username' ? '✓' : '⎘'}
          </button>
        </div>
      </div>

      {/* Password */}
      <div className="pw-field">
        <label>Password</label>
        <div className="pw-input-row">
          <input
            type={showPassword ? 'text' : 'password'}
            value={content.password}
            placeholder="Enter or generate a password"
            onChange={(e) => update({ password: e.target.value })}
            autoComplete="new-password"
            className="pw-password-input"
          />
          <button
            className="pw-action-btn"
            onClick={() => setShowPassword((v) => !v)}
            title={showPassword ? 'Hide password' : 'Show password'}
          >
            {showPassword ? '🙈' : '👁'}
          </button>
          <button
            className={`pw-action-btn ${copied === 'password' ? 'pw-action-btn--copied' : ''}`}
            onClick={() => handleCopy('password')}
            title="Copy password"
            disabled={!content.password}
          >
            {copied === 'password' ? '✓' : '⎘'}
          </button>
          <button className="pw-action-btn pw-generate-btn" onClick={handleGenerate} title="Generate strong password">
            ⚡
          </button>
        </div>
        {content.password && (
          <div className="pw-strength">
            <div className={`pw-strength-bar pw-strength-bar--${passwordStrength.level}`}>
              <div className="pw-strength-fill" style={{ width: `${passwordStrength.score}%` }} />
            </div>
            <span className={`pw-strength-label pw-strength-label--${passwordStrength.level}`}>
              {passwordStrength.label}
            </span>
          </div>
        )}
      </div>

      {/* TOTP */}
      <div className="pw-field">
        <label>TOTP Secret <span className="pw-optional">(optional)</span></label>
        <input
          value={content.totp ?? ''}
          placeholder="Base32 secret key"
          onChange={(e) => update({ totp: e.target.value || undefined })}
          autoComplete="off"
        />
      </div>

      {/* Notes */}
      <div className="pw-field pw-field--grow">
        <label>Notes</label>
        <textarea
          value={content.notes}
          placeholder="Additional notes..."
          rows={4}
          onChange={(e) => update({ notes: e.target.value })}
        />
      </div>
    </div>
  );
};

function getPasswordStrength(password: string): { score: number; level: 'weak' | 'fair' | 'strong' | 'very-strong'; label: string } {
  if (!password) return { score: 0, level: 'weak', label: '' };

  let score = 0;
  if (password.length >= 8) score += 20;
  if (password.length >= 16) score += 20;
  if (password.length >= 20) score += 10;
  if (/[a-z]/.test(password)) score += 10;
  if (/[A-Z]/.test(password)) score += 10;
  if (/[0-9]/.test(password)) score += 10;
  if (/[^a-zA-Z0-9]/.test(password)) score += 20;

  if (score < 40) return { score, level: 'weak', label: 'Weak' };
  if (score < 60) return { score, level: 'fair', label: 'Fair' };
  if (score < 80) return { score, level: 'strong', label: 'Strong' };
  return { score, level: 'very-strong', label: 'Very strong' };
}

export default PasswordEditor;
