/**
 * Published language between bounded contexts: the integration events that cross a context
 * boundary. Inside the monolith they travel as Spring application events delivered after the
 * publishing transaction commits; when a context is extracted, the ports that publish them are
 * implemented with HTTP calls instead. See docs/integration-map.md.
 */
package com.fundatechperu.shareway.shared.integration;
