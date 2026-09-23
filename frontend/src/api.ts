import { SimulationParameters, SimulationRequest, SimulationResponse } from './types.js';

/**
 * Base URL of the Java API. The front end is served on its own port during development, but the
 * same code works when the built files are served by the back end itself.
 */
const API_BASE_URL =
  window.location.port === '8080' ? '/api' : 'http://localhost:8080/api';

export class ApiError extends Error {}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(API_BASE_URL + path, init);
  } catch (cause) {
    throw new ApiError(
      `Unable to reach the simulation API on ${API_BASE_URL}. Is the Java back end started?`,
    );
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const message = body && typeof body.message === 'string' ? body.message : response.statusText;
    throw new ApiError(message);
  }
  return (await response.json()) as T;
}

/** Default parameters, read by the back end from its configuration file. */
export function fetchConfiguration(): Promise<SimulationParameters> {
  return request<SimulationParameters>('/configuration');
}

/** Runs a complete simulation and returns every intermediate state. */
export function runSimulation(parameters: SimulationRequest): Promise<SimulationResponse> {
  return request<SimulationResponse>('/simulations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(parameters),
  });
}
