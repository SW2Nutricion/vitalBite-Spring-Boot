# Integración con n8n

n8n debe obtener un token de servicio mediante `client_credentials` usando un
cliente con rol `SERVICE`. El workflow periódico:

1. Obtiene el token del IdP.
2. Ejecuta `POST /api/v1/documentos` con `Authorization: Bearer ...`,
   `X-Saga-Id` e `Idempotency-Key`.
3. Notifica el resultado al core o al paciente.
4. Reintenta hasta tres veces con la misma clave; después solicita la
   compensación definida en [saga.md](saga.md).

El cliente del core sigue el mismo flujo y ambos clientes deben tener
credenciales independientes y secretos fuera del repositorio.
