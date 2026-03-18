import React, { useState } from 'react';
import './EncryptionPrompt.css';

interface Props {
  categoryName: string;
  passwordHint?: string;
  onUnlock: (password: string) => Promise<void>;
}

const EncryptionPrompt: React.FC<Props> = ({ categoryName, passwordHint, onUnlock }) => {
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!password) return;
    setLoading(true);
    setError('');
    try {
      await onUnlock(password);
    } catch {
      setError('Incorrect password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="encryption-prompt">
      <div className="encryption-card">
        <div className="encryption-icon">🔒</div>
        <h2>Encrypted Category</h2>
        <p>Enter the password to unlock <strong>{categoryName}</strong></p>

        <form onSubmit={handleSubmit}>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Category password"
            autoFocus
          />
          {passwordHint && (
            <p className="encryption-hint">💡 Hint: {passwordHint}</p>
          )}
          {error && <p className="encryption-error">{error}</p>}
          <button type="submit" disabled={loading || !password}>
            {loading ? 'Unlocking…' : 'Unlock'}
          </button>
        </form>
        <p className="encryption-note">
          The password never leaves your device — decryption happens locally.
        </p>
      </div>
    </div>
  );
};

export default EncryptionPrompt;
