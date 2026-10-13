import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import './styles.css';
import 'leaflet/dist/leaflet.css';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }
  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }
  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '40px', maxWidth: '600px', margin: '60px auto', background: '#fff', borderRadius: '12px', boxShadow: '0 4px 20px rgba(0,0,0,0.08)', fontFamily: 'sans-serif', textAlign: 'center' }}>
          <h2 style={{ color: '#cb4f39' }}>Something went wrong</h2>
          <p style={{ color: '#555', fontSize: '14px' }}>{this.state.error?.message || 'An unexpected error occurred.'}</p>
          <div style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <button style={{ padding: '10px 18px', background: '#286fb4', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }} onClick={() => window.location.reload()}>Reload Page</button>
            <button style={{ padding: '10px 18px', background: '#f5f7fa', color: '#333', border: '1px solid #ccc', borderRadius: '6px', cursor: 'pointer' }} onClick={() => { localStorage.clear(); window.location.href = '/'; }}>Reset & Sign In</button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ErrorBoundary>
  </React.StrictMode>
);
