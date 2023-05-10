import { api, oa3server, oa3serverVariables } from "@airtasker/spot";

@api({ name: "Prom4j OpenAPI", version: "0.0.1-SNAPSHOT" })
class Api {
  @oa3server({ name: 'Prom4j', url: "/openapi" })
  openapi() {}
}

// Prom4j API
import './prom4j/endpoints';
