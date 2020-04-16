output "idam_api_base_uri" {
  value = "${var.idam_api_base_uri}"
}

output "open_id_api_base_uri" {
  value = "${var.open_id_api_base_uri}"
}

output "s2s_base_uri" {
  value = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "idam_webshow_whitelist" {
  value = "https://em-show-aat.service.core-compute-aat.internal/oauth2/callback"
}

output "enable_idam_healthcheck" {
  value = "${var.enable_idam_healthcheck}"
}

output "dm_store_app_url" {
  value = "http://${var.dm_store_app_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "em_stitching_api_url" {
  value = "http://${var.em_stitching_api_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}
