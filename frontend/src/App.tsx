import { useEffect, useState } from 'react'

interface HelloResponse {
  message: string
  timestamp: string
  status: string
}

function App() {
  const [data, setData] = useState<HelloResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/hello')
      .then(res => res.json())
      .then(data => {
        setData(data)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'system-ui, -apple-system, sans-serif',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <div style={{
        background: 'white',
        padding: '3rem',
        borderRadius: '1rem',
        boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
        maxWidth: '500px',
        textAlign: 'center'
      }}>
        <h1 style={{
          fontSize: '2.5rem',
          marginBottom: '1rem',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent'
        }}>
          LightBoard
        </h1>

        {loading && <p>Loading...</p>}

        {error && (
          <div style={{ color: '#ef4444', padding: '1rem', background: '#fee2e2', borderRadius: '0.5rem' }}>
            <strong>Error:</strong> {error}
          </div>
        )}

        {data && (
          <div style={{ marginTop: '2rem' }}>
            <div style={{
              background: '#f0fdf4',
              border: '2px solid #86efac',
              borderRadius: '0.5rem',
              padding: '1.5rem',
              marginBottom: '1rem'
            }}>
              <h2 style={{ color: '#15803d', marginBottom: '0.5rem' }}>
                {data.message}
              </h2>
              <p style={{ color: '#16a34a', fontSize: '0.875rem' }}>
                Status: {data.status}
              </p>
              <p style={{ color: '#666', fontSize: '0.75rem', marginTop: '0.5rem' }}>
                {data.timestamp}
              </p>
            </div>

            <p style={{ color: '#666', fontSize: '0.875rem' }}>
              Frontend and Backend are successfully connected!
            </p>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
