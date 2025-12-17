terraform {

  backend "http" {
  }

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.0"
    }
  }
}

locals {
  namespace = "fetch"
}

provider "kubernetes" {
  # config_path = "~/.kube/config"
  config_path = "${path.module}/kubeconfig"
}

variable "image" {}
variable "name" {}
variable "volume_size" {}
variable "storage_class_name" {}


module "k8s_postgres" {
  source            = "./modules/postgres"
  name              = var.name
  namespace         = local.namespace
  image             = var.image
  volume_size       = var.volume_size
  storage_class_name = var.storage_class_name
  image_pull_secrets = "gitlab-database-2024"
}
