provider "azurerm" {
    features {}
}

locals {
  app_full_name       = "${var.product}-${var.component}"
  ase_name            = "core-compute-${var.env}"
  local_env           = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  shared_vault_name   = "${local.app_full_name}-${local.local_env}"
  s2s_key             = data.azurerm_key_vault_secret.s2s_key.value
  resource_group_name = "${local.app_full_name}-${var.env}"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

resource "azurerm_resource_group" "rg" {
  name     = local.resource_group_name
  location = var.location

  tags = var.common_tags
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_key" {
  name         = "microservicekey-em-ccd-orchestrator"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

module "local_key_vault" {
  source                     = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product                    = local.app_full_name
  env                        = var.env
  tenant_id                  = var.tenant_id
  object_id                  = var.jenkins_AAD_objectId
  resource_group_name        = azurerm_resource_group.rg.name
  product_group_object_id    = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags                = var.common_tags
  managed_identity_object_ids = ["${data.azurerm_user_assigned_identity.rpa-shared-identity.principal_id}"]
}

data "azurerm_user_assigned_identity" "rpa-shared-identity" {
  name                = "rpa-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

resource "azurerm_key_vault_secret" "local_s2s_key" {
  name         = "microservicekey-em-ccd-orchestrator"
  value        = data.azurerm_key_vault_secret.s2s_key.value
  key_vault_id = module.local_key_vault.key_vault_id
}

data "azurerm_application_insights" "appinsights" {
  name                = "rpa-${var.env}"
  resource_group_name = "rpa-${var.env}"
}
resource "azurerm_key_vault_secret" "appinsights_key" {
  name         = "AppInsightsInstrumentationKey"
  value        = data.azurerm_application_insights.appinsights.instrumentation_key
  key_vault_id = module.local_key_vault.key_vault_id
}