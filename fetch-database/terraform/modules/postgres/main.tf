variable name {}
variable namespace {}
variable image {}
variable volume_size {}
variable storage_class_name {}
variable image_pull_secrets {}


resource "kubernetes_stateful_set" "postgres" {
  metadata {
    name      = var.name
    namespace = var.namespace
  }
  spec {
    selector {
      match_labels = {
        app = var.name
      }
    }
    service_name = var.name
    replicas     = 1
    template {
      metadata {
        labels = {
          app = var.name
        }
      }
      spec {
        container {
          name  = var.name
          image = var.image
          port {
            container_port = 5432
          }
          volume_mount {
            name       = "postgres-storage"
            mount_path = "/var/lib/postgresql/data"
          }
        }
        image_pull_secrets {
          name = var.image_pull_secrets
        }
      }
    }
    volume_claim_template {
      metadata {
        name = "postgres-storage"
      }
      spec {
        access_modes = ["ReadWriteOnce"]
        resources {
          requests = {
            storage = var.volume_size
          }
        }
        storage_class_name = var.storage_class_name
      }
    }
  }
}

resource "kubernetes_service" "postgres" {
  metadata {
    name      = var.name
    namespace = var.namespace
  }
  spec {
    selector = {
      app = var.name
    }
    port {
      port        = 5432
      target_port = 5432
    }
    type = "ClusterIP"
  }
}
