const FACEIT_BASE_URL = "https://open.faceit.com/data/v4";

type ErrorBody = {
  error: string;
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return corsResponse(null, 204);
  }

  if (request.method !== "GET") {
    return jsonResponse({ error: "method not allowed" }, 405);
  }

  const apiKey = Deno.env.get("FACEIT_API_KEY");
  if (!apiKey) {
    return jsonResponse({ error: "FACEIT_API_KEY is not configured" }, 500);
  }

  const url = new URL(request.url);
  const nickname = url.searchParams.get("nickname")?.trim();
  if (!nickname) {
    return jsonResponse({ error: "nickname is required" }, 400);
  }

  const faceitUrl = new URL(`${FACEIT_BASE_URL}/players`);
  faceitUrl.searchParams.set("nickname", nickname);
  faceitUrl.searchParams.set("game", "cs2");

  return proxyFaceit(faceitUrl, apiKey);
});

async function proxyFaceit(url: URL, apiKey: string): Promise<Response> {
  const response = await fetch(url, {
    headers: {
      "Accept": "application/json",
      "Authorization": `Bearer ${apiKey}`,
    },
  });

  const body = await response.text();
  return corsResponse(body, response.status, response.headers.get("content-type") ?? "application/json");
}

function jsonResponse(body: ErrorBody, status: number): Response {
  return corsResponse(JSON.stringify(body), status, "application/json");
}

function corsResponse(
  body: BodyInit | null,
  status: number,
  contentType = "application/json",
): Response {
  return new Response(body, {
    status,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Content-Type": contentType,
    },
  });
}
