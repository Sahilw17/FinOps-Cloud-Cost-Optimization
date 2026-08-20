import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api/client';
import { Sparkles, Send, X, Bot, User, Copy, Check, ArrowRight, Lightbulb, Zap, Shield, RefreshCw } from 'lucide-react';

export default function AiCopilotDrawer({ isOpen, onClose, selectedAccountId }) {
  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      sender: 'ai',
      text: "👋 **Hello! I'm your FinOps AI Copilot.**\n\nI have real-time visibility into your linked AWS accounts, resource configurations, CloudWatch telemetry streams, and historical billing records.\n\nHow can I help optimize your cloud spend today?",
      modelUsed: 'Google Gemini 2.0 Flash',
      followUps: [
        "How can we cut our AWS spend by 30% without downtime?",
        "Which instances are safe to shut down on weekends?",
        "Generate Terraform code for Graviton3 & gp3 migration",
        "Summarize our biggest cloud waste across accounts"
      ]
    }
  ]);
  const [inputQuery, setInputQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [copiedIndex, setCopiedIndex] = useState(null);
  const chatEndRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen]);

  if (!isOpen) return null;

  const handleSend = async (queryText) => {
    const textToSend = queryText || inputQuery;
    if (!textToSend.trim() || loading) return;

    const userMsg = {
      id: 'user-' + Date.now(),
      sender: 'user',
      text: textToSend
    };

    setMessages(prev => [...prev, userMsg]);
    setInputQuery('');
    setLoading(true);

    try {
      const resp = await api.aiChat(textToSend, selectedAccountId ? parseInt(selectedAccountId) : null, null);
      const aiMsg = {
        id: 'ai-' + Date.now(),
        sender: 'ai',
        text: resp.answer || 'Analysis complete.',
        modelUsed: resp.modelUsed || 'Google Gemini 2.0 Flash',
        followUps: resp.followUpQuestions || [],
        actions: resp.recommendedActions || []
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          id: 'ai-err-' + Date.now(),
          sender: 'ai',
          text: `⚠️ **AI Copilot Notice**: ${err.message || 'Unable to connect to AI reasoning service. Please check backend connection.'}`,
          modelUsed: 'Fallback Engine'
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text, idx) => {
    navigator.clipboard.writeText(text);
    setCopiedIndex(idx);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  // Helper to format markdown text with code snippets
  const renderMessageContent = (text, msgIndex) => {
    const parts = text.split(/(```[\s\S]*?```)/g);
    return parts.map((part, pIdx) => {
      if (part.startsWith('```')) {
        const lines = part.slice(3, -3).trim().split('\n');
        const lang = lines[0].trim();
        const code = (lang && !lang.includes(' ') && lines.length > 1) ? lines.slice(1).join('\n') : lines.join('\n');
        const blockId = `${msgIndex}-${pIdx}`;

        return (
          <div key={pIdx} style={{
            margin: '10px 0',
            background: 'rgba(15, 23, 42, 0.95)',
            border: '1px solid rgba(56, 189, 248, 0.25)',
            borderRadius: 8,
            overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '6px 12px',
              background: 'rgba(56, 189, 248, 0.1)',
              borderBottom: '1px solid rgba(56, 189, 248, 0.2)',
              fontSize: '0.74rem',
              color: 'var(--primary)',
              fontFamily: 'var(--font-mono)'
            }}>
              <span>{lang || 'code'}</span>
              <button
                onClick={() => copyToClipboard(code, blockId)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: copiedIndex === blockId ? 'var(--success)' : 'var(--text-muted)',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 4,
                  fontSize: '0.72rem'
                }}
              >
                {copiedIndex === blockId ? <><Check size={12} /> Copied</> : <><Copy size={12} /> Copy Code</>}
              </button>
            </div>
            <pre style={{
              margin: 0,
              padding: '10px 12px',
              fontSize: '0.78rem',
              fontFamily: 'var(--font-mono)',
              overflowX: 'auto',
              color: '#F8FAFC',
              lineHeight: 1.45
            }}>
              <code>{code}</code>
            </pre>
          </div>
        );
      }

      // Simple markdown bold and bullet rendering
      const formattedLines = part.split('\n').map((line, lIdx) => {
        let content = line;
        if (line.startsWith('### ')) {
          return <h4 key={lIdx} style={{ fontSize: '0.95rem', fontWeight: 700, margin: '8px 0 4px', color: 'var(--primary)' }}>{line.slice(4)}</h4>;
        }
        if (line.startsWith('## ')) {
          return <h3 key={lIdx} style={{ fontSize: '1.05rem', fontWeight: 700, margin: '10px 0 6px' }}>{line.slice(3)}</h3>;
        }
        if (line.startsWith('- ') || line.startsWith('* ')) {
          return (
            <div key={lIdx} style={{ display: 'flex', gap: 6, margin: '3px 0', paddingLeft: 6 }}>
              <span style={{ color: 'var(--primary)' }}>•</span>
              <span>{renderInlineStyles(line.slice(2))}</span>
            </div>
          );
        }
        return <p key={lIdx} style={{ margin: '4px 0' }}>{renderInlineStyles(content)}</p>;
      });

      return <div key={pIdx}>{formattedLines}</div>;
    });
  };

  const renderInlineStyles = (str) => {
    // replace **bold** with <strong>
    const boldParts = str.split(/(\*\*.*?\*\*)/g);
    return boldParts.map((bPart, bIdx) => {
      if (bPart.startsWith('**') && bPart.endsWith('**')) {
        return <strong key={bIdx} style={{ color: '#fff' }}>{bPart.slice(2, -2)}</strong>;
      }
      // replace `code` with <code>
      const codeParts = bPart.split(/(`.*?`)/g);
      return codeParts.map((cPart, cIdx) => {
        if (cPart.startsWith('`') && cPart.endsWith('`')) {
          return <code key={cIdx} style={{ background: 'rgba(255,255,255,0.1)', padding: '1px 5px', borderRadius: 4, fontFamily: 'var(--font-mono)', fontSize: '0.82em', color: 'var(--primary)' }}>{cPart.slice(1, -1)}</code>;
        }
        return cPart;
      });
    });
  };

  return (
    <>
      <div
        className="modal-backdrop"
        style={{ zIndex: 1050, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(3px)' }}
        onClick={onClose}
      />
      <div style={{
        position: 'fixed',
        top: 0,
        right: 0,
        bottom: 0,
        width: '100%',
        maxWidth: 520,
        background: '#0B1120',
        borderLeft: '1px solid rgba(56, 189, 248, 0.25)',
        boxShadow: '-8px 0 32px rgba(0, 0, 0, 0.6)',
        zIndex: 1060,
        display: 'flex',
        flexDirection: 'column',
        animation: 'slideInRight 0.25s ease-out'
      }}>
        {/* Drawer Header */}
        <div style={{
          padding: '16px 20px',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: 'linear-gradient(180deg, rgba(15, 23, 42, 0.9) 0%, #0B1120 100%)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 34,
              height: 34,
              borderRadius: 8,
              background: 'linear-gradient(135deg, #38BDF8, #6366F1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 16px rgba(56, 189, 248, 0.4)'
            }}>
              <Sparkles size={18} color="#fff" />
            </div>
            <div>
              <div style={{ fontSize: '0.96rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 6 }}>
                FinOps AI Copilot
                <span className="pill pill-primary" style={{ fontSize: '0.65rem', padding: '1px 5px' }}>
                  Live LLM
                </span>
              </div>
              <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                Powered by Google Gemini 2.0 Flash & Grounded Cloud Telemetry
              </div>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: 4 }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Message History */}
        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '20px',
          display: 'flex',
          flexDirection: 'column',
          gap: 16
        }}>
          {messages.map((m, idx) => (
            <div
              key={m.id}
              style={{
                display: 'flex',
                gap: 10,
                alignSelf: m.sender === 'user' ? 'flex-end' : 'flex-start',
                maxWidth: m.sender === 'user' ? '85%' : '100%'
              }}
            >
              {m.sender === 'ai' && (
                <div style={{
                  width: 28,
                  height: 28,
                  borderRadius: 6,
                  background: 'rgba(56, 189, 248, 0.15)',
                  border: '1px solid rgba(56, 189, 248, 0.3)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  marginTop: 2
                }}>
                  <Bot size={16} color="var(--primary)" />
                </div>
              )}

              <div style={{
                background: m.sender === 'user' ? 'linear-gradient(135deg, #0284C7, #4F46E5)' : 'rgba(30, 41, 59, 0.65)',
                border: m.sender === 'user' ? 'none' : '1px solid var(--border-subtle)',
                borderRadius: 10,
                padding: '12px 14px',
                fontSize: '0.84rem',
                lineHeight: 1.5,
                color: '#E2E8F0',
                boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
              }}>
                {renderMessageContent(m.text, idx)}

                {/* Follow up question suggestion chips */}
                {m.followUps && m.followUps.length > 0 && (
                  <div style={{ marginTop: 12, paddingTop: 10, borderTop: '1px solid rgba(255,255,255,0.08)' }}>
                    <div style={{ fontSize: '0.72rem', color: 'var(--primary)', marginBottom: 6, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 4 }}>
                      <Lightbulb size={12} /> Suggested Inquiries:
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      {m.followUps.map((fu, fIdx) => (
                        <button
                          key={fIdx}
                          disabled={loading}
                          onClick={() => handleSend(fu)}
                          style={{
                            background: 'rgba(56, 189, 248, 0.08)',
                            border: '1px solid rgba(56, 189, 248, 0.25)',
                            borderRadius: 6,
                            padding: '5px 10px',
                            color: '#BAE6FD',
                            fontSize: '0.76rem',
                            textAlign: 'left',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            transition: 'all 0.15s ease'
                          }}
                        >
                          <span>{fu}</span>
                          <ArrowRight size={12} color="var(--primary)" />
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {m.sender === 'user' && (
                <div style={{
                  width: 28,
                  height: 28,
                  borderRadius: 6,
                  background: 'linear-gradient(135deg, #38BDF8, #6366F1)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  marginTop: 2
                }}>
                  <User size={16} color="#fff" />
                </div>
              )}
            </div>
          ))}

          {loading && (
            <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
              <div style={{
                width: 28,
                height: 28,
                borderRadius: 6,
                background: 'rgba(56, 189, 248, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Sparkles size={16} color="var(--primary)" className="spin" />
              </div>
              <div style={{
                background: 'rgba(30, 41, 59, 0.65)',
                border: '1px solid var(--border-subtle)',
                borderRadius: 10,
                padding: '10px 14px',
                fontSize: '0.8rem',
                color: 'var(--text-muted)',
                display: 'flex',
                alignItems: 'center',
                gap: 8
              }}>
                <span>Synthesizing CloudWatch metrics & generating FinOps recommendation...</span>
              </div>
            </div>
          )}

          <div ref={chatEndRef} />
        </div>

        {/* Input Controls */}
        <div style={{
          padding: '14px 20px',
          borderTop: '1px solid var(--border-subtle)',
          background: 'rgba(15, 23, 42, 0.85)'
        }}>
          <form
            onSubmit={(e) => { e.preventDefault(); handleSend(); }}
            style={{ display: 'flex', gap: 8 }}
          >
            <input
              type="text"
              className="form-input"
              style={{ flex: 1, padding: '10px 14px', fontSize: '0.84rem' }}
              placeholder="Ask FinOps AI (e.g. 'How to reduce EC2 cost by 20%?')..."
              value={inputQuery}
              onChange={(e) => setInputQuery(e.target.value)}
              disabled={loading}
            />
            <button
              type="submit"
              disabled={loading || !inputQuery.trim()}
              className="btn btn-primary"
              style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 6 }}
            >
              <Send size={15} />
            </button>
          </form>
        </div>
      </div>
    </>
  );
}
