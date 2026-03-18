import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../../store';
import { login, register } from '../../store/authSlice';
import { authApi } from '../../api/authApi';
import './LoginPage.css';

/** Safe icon — rectangular vault body with the site's combination-dial logo as the door lock. */
const SafeIcon: React.FC = () => (
  <svg viewBox="0 0 72 72" fill="none" xmlns="http://www.w3.org/2000/svg" className="login-safe-icon" aria-hidden="true">
    {/* Safe outer body */}
    <rect x="3" y="3" width="66" height="66" rx="10" fill="#13131f" stroke="#7c3aed" strokeWidth="2.5"/>

    {/* Left hinges */}
    <rect x="3" y="17" width="9" height="10" rx="3" fill="#7c3aed"/>
    <rect x="3" y="45" width="9" height="10" rx="3" fill="#7c3aed"/>

    {/* Door face panel */}
    <rect x="14" y="9" width="48" height="54" rx="7" stroke="#7c3aed" strokeWidth="1.5" fill="rgba(124,58,237,0.06)"/>

    {/* Door handle (right edge) */}
    <rect x="60" y="29" width="7" height="14" rx="3.5" fill="#7c3aed"/>

    {/* ── Vault combination dial (site logo, centered on door at 38,36) ── */}
    {/* Outer ring */}
    <circle cx="38" cy="36" r="12" stroke="#7c3aed" strokeWidth="2"/>

    {/* Four locking bolts at cardinal positions */}
    <circle cx="38" cy="22.6" r="2.2" fill="#7c3aed"/>
    <circle cx="51.4" cy="36"  r="2.2" fill="#7c3aed"/>
    <circle cx="38" cy="49.4" r="2.2" fill="#7c3aed"/>
    <circle cx="24.6" cy="36"  r="2.2" fill="#7c3aed"/>

    {/* Inner combination ring */}
    <circle cx="38" cy="36" r="6.5" stroke="#7c3aed" strokeWidth="1.5"/>

    {/* Center hub */}
    <circle cx="38" cy="36" r="2.5" fill="#7c3aed"/>

    {/* Dial indicator (~2 o'clock, partially opened) */}
    <line x1="38" y1="36" x2="42.7" y2="33.3" stroke="#a78bfa" strokeWidth="1.8" strokeLinecap="round"/>
  </svg>
);

const LoginPage: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { status, error } = useSelector((s: RootState) => s.auth);
  const [mode, setMode] = useState<'login' | 'register' | 'recover'>('login');
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [confirmPassword, setConfirmPassword] = useState('');

  // recover mode state
  const [recoverForm, setRecoverForm] = useState({ username: '', email: '', newPassword: '', confirmPassword: '' });
  const [recoverStatus, setRecoverStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [recoverError, setRecoverError] = useState('');

  const passwordMismatch =
    mode === 'register' && confirmPassword.length > 0 && form.password !== confirmPassword;
  const confirmEmpty = mode === 'register' && confirmPassword.length === 0;

  const recoverMismatch = recoverForm.confirmPassword.length > 0 && recoverForm.newPassword !== recoverForm.confirmPassword;
  const recoverIncomplete = !recoverForm.username || !recoverForm.email || !recoverForm.newPassword || recoverForm.newPassword !== recoverForm.confirmPassword;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (mode === 'login') {
      dispatch(login({ username: form.username, password: form.password }));
    } else {
      if (form.password !== confirmPassword) return;
      dispatch(register(form));
    }
  };

  const handleRecover = async (e: React.FormEvent) => {
    e.preventDefault();
    if (recoverIncomplete) return;
    setRecoverStatus('loading');
    setRecoverError('');
    try {
      await authApi.resetPassword({
        username: recoverForm.username,
        email: recoverForm.email,
        newPassword: recoverForm.newPassword,
      });
      setRecoverStatus('success');
    } catch (err: any) {
      const msg = err?.response?.data || 'Unable to reset password. Check your username and email.';
      setRecoverError(typeof msg === 'string' ? msg : JSON.stringify(msg));
      setRecoverStatus('error');
    }
  };

  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));
  const updateRecover = (field: string, value: string) => setRecoverForm((f) => ({ ...f, [field]: value }));

  const switchMode = (next: 'login' | 'register' | 'recover') => {
    setMode(next);
    setConfirmPassword('');
    setRecoverStatus('idle');
    setRecoverError('');
  };

  if (mode === 'recover') {
    return (
      <div className="login-page">
        <div className="login-card">
          <div className="login-logo"><SafeIcon /></div>
          <h1>Legacy Vault</h1>
          <p className="login-tagline">Reset your password</p>

          {recoverStatus === 'success' ? (
            <div className="recover-success">
              <p>Password reset successfully.</p>
              <button className="login-btn" onClick={() => switchMode('login')}>Back to Sign In</button>
            </div>
          ) : (
            <form onSubmit={handleRecover}>
              <div className="form-field">
                <label>Username</label>
                <input value={recoverForm.username} onChange={(e) => updateRecover('username', e.target.value)}
                  placeholder="Your username" autoComplete="username" required />
              </div>
              <div className="form-field">
                <label>Email</label>
                <input type="email" value={recoverForm.email} onChange={(e) => updateRecover('email', e.target.value)}
                  placeholder="Account email address" autoComplete="email" required />
              </div>
              <div className="form-field">
                <label>New Password</label>
                <input type="password" value={recoverForm.newPassword} onChange={(e) => updateRecover('newPassword', e.target.value)}
                  placeholder="New password (min 8 chars)" autoComplete="new-password" required minLength={8} />
              </div>
              <div className="form-field">
                <label>Confirm New Password</label>
                <input
                  type="password"
                  value={recoverForm.confirmPassword}
                  onChange={(e) => updateRecover('confirmPassword', e.target.value)}
                  placeholder="Re-enter new password"
                  autoComplete="new-password"
                  required
                  className={recoverMismatch ? 'login-input--error' : ''}
                />
                {recoverMismatch && (
                  <span className="login-field-error">Passwords do not match</span>
                )}
              </div>

              {recoverStatus === 'error' && <p className="login-error">{recoverError}</p>}

              <button type="submit" className="login-btn" disabled={recoverStatus === 'loading' || recoverIncomplete}>
                {recoverStatus === 'loading' ? 'Resetting…' : 'Reset Password'}
              </button>
              <button type="button" className="login-btn-link" onClick={() => switchMode('login')}>
                Back to Sign In
              </button>
            </form>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo"><SafeIcon /></div>
        <h1>Legacy Vault</h1>
        <p className="login-tagline">What is your legacy?</p>

        <div className="login-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => switchMode('login')}>
            Sign In
          </button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => switchMode('register')}>
            Create Account
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-field">
            <label>Username</label>
            <input value={form.username} onChange={(e) => update('username', e.target.value)}
              placeholder="Username" autoComplete="username" required />
          </div>
          {mode === 'register' && (
            <div className="form-field">
              <label>Email</label>
              <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)}
                placeholder="Email address" autoComplete="email" required />
            </div>
          )}
          <div className="form-field">
            <label>Password</label>
            <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)}
              placeholder="Password" autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
              required minLength={8} />
          </div>
          {mode === 'register' && (
            <div className="form-field">
              <label>Confirm Password</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Re-enter password"
                autoComplete="new-password"
                required
                className={passwordMismatch ? 'login-input--error' : ''}
              />
              {passwordMismatch && (
                <span className="login-field-error">Passwords do not match</span>
              )}
            </div>
          )}

          {error && <p className="login-error">{String(error)}</p>}

          <button
            type="submit"
            disabled={status === 'loading' || passwordMismatch || (mode === 'register' && confirmEmpty)}
            className="login-btn"
          >
            {status === 'loading' ? 'Please wait…' : mode === 'login' ? 'Sign In' : 'Create Account'}
          </button>
          {mode === 'login' && (
            <button type="button" className="login-btn-link" onClick={() => switchMode('recover')}>
              Forgot password?
            </button>
          )}
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
