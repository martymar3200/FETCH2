const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    // We build our routes based on the containing layout component so all pages that will live under MainLayout need to be child paths
    // Keep routes sorted alphabetically by path
    children: [
      {
        name: 'home',
        path: '',
        component: () => import('@/pages/IndexPage.vue'),
        meta: {
          title: 'Home'
        }
      },
      {
        name: 'accession',
        path: 'accession/:jobId?', // child path reads as "parent path + / + child_path" ex: /accession
        component: () => import('@/pages/AccessionPage.vue'),
        meta: {
          title: 'Accession',
          requiresAuth: true,
          requiresPerm: 'can_access_accession'
        }
      },
      {
        name: 'accession-container',
        path: 'accession/:jobId?/scan-items/:containerId?',
        component: () => import('@/pages/AccessionPage.vue'),
        beforeEnter ({ params }) {
          if (!params.containerId) {
            return {
              name: 'accession',
              params: {
                jobId: params.jobId
              }
            }
          }
        },
        meta: {
          title: 'Accession Container',
          requiresAuth: true,
          requiresPerm: 'can_access_accession'
        }
      },
      {
        name: 'admin-home',
        path: 'admin',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Admin',
          requiresAuth: true,
          requiresPerm: 'can_access_admin'
        }
      },
      {
        name: 'admin-groups',
        path: 'admin/groups/:groupId?',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Admin Groups',
          requiresAuth: true,
          requiresPerm: 'can_manage_groups_and_permissions'
        }
      },
      {
        name: 'admin-location-explorer',
        path: 'admin/locations',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Admin Locations',
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-manage-owner',
        path: 'admin/manage/owner',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Owners',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-media-type',
        path: 'admin/manage/media-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Media Types',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-size-class',
        path: 'admin/manage/size-class',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Size Classes',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-shelf-type',
        path: 'admin/manage/shelf-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Shelf Types',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-priority',
        path: 'admin/manage/priority',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Priorities',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-delivery-location',
        path: 'admin/manage/delivery-location',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Delivery Locations',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-request-type',
        path: 'admin/manage/request-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Request Types',
          requiresAuth: true,
          requiresPerm: 'can_manage_list_configurations'
        }
      },
      {
        name: 'admin-manage-barcode-type',
        path: 'admin/manage/barcode-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Barcode Types',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-manage-manual-request-settings',
        path: 'admin/manage/manual-request-settings',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manual Request Settings',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-manage-ils',
        path: 'admin/manage/ils',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage ILS',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-integration-issues',
        path: 'admin/manage/ils/issues',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Integration Issues',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-manage-shelf-position-direction',
        path: 'admin/manage/shelf-position-direction',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Shelf Position Direction',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-manage-shipping',
        path: 'admin/manage/shipping',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Shipping Settings',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-manage-child-owner-shelving',
        path: 'admin/manage/child-owner-shelving',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Manage Child Owner Shelving',
          requiresAuth: true,
          requiresPerm: 'can_manage_system_configurations'
        }
      },
      {
        name: 'admin-users',
        path: 'admin/users',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          title: 'Admin Users',
          requiresAuth: true,
          requiresPerm: 'can_manage_groups_and_permissions'
        }
      },
      {
        name: 'record-management-items',
        path: 'record-management/items/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          title: 'Item Record Management',
          requiresAuth: true,
          requiresPerm: 'can_access_item_detail'
        }
      },
      {
        name: 'record-management-shelf',
        path: 'record-management/shelf/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          title: 'Shelf Record Management',
          requiresAuth: true,
          requiresPerm: 'can_access_shelf_detail'
        }
      },
      {
        name: 'record-management-tray',
        path: 'record-management/tray/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          title: 'Tray Record Management',
          requiresAuth: true,
          requiresPerm: 'can_access_tray_detail'
        }
      },
      {
        name: 'picklist',
        path: 'picklist/:jobId?',
        component: () => import('@/pages/PicklistPage.vue'),
        meta: {
          title: 'Pick List',
          requiresAuth: true,
          requiresPerm: 'can_access_picklist'
        }
      },
      {
        name: 'refile',
        path: 'refile/:jobId?',
        component: () => import('@/pages/RefilePage.vue'),
        meta: {
          title: 'Refile',
          requiresAuth: true,
          requiresPerm: 'can_access_refile'
        }
      },
      {
        name: 'reports',
        path: 'reports/:reportType?',
        component: () => import('@/pages/ReportsPage.vue'),
        meta: {
          title: 'Reports',
          requiresAuth: true,
          requiresPerm: 'can_access_reports'
        }
      },
      {
        name: 'request',
        path: 'request',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          title: 'Requests',
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'request-details',
        path: 'request/details/:jobId',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          title: 'Request Details',
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'request-batch',
        path: 'request/batch/:jobId',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          title: 'Batch Request Details',
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'search-results',
        path: 'search-results/:searchType?',
        component: () => import('@/pages/SearchPage.vue'),
        meta: {
          title: 'Search Results'
        }
      },
      {
        name: 'shelving',
        path: 'shelving/:jobId?',
        component: () => import('@/pages/ShelvingPage.vue'),
        meta: {
          title: 'Shelving',
          requiresAuth: true,
          requiresPerm: 'can_access_shelving'
        }
      },
      {
        name: 'shelving-dts',
        path: 'shelving/direct-to-shelve/:jobId?',
        component: () => import('@/pages/ShelvingPage.vue'),
        meta: {
          title: 'Direct to Shelf',
          requiresAuth: true,
          requiresPerm: 'can_access_shelving'
        }
      },
      {
        name: 'shipping',
        path: 'shipping',
        component: () => import('@/pages/ShippingPage.vue'),
        meta: {
          title: 'Shipping',
          requiresAuth: true,
          requiresPerm: 'can_access_shipping'
        }
      },
      {
        name: 'shipping-execute',
        path: 'shipping/:jobId',
        component: () => import('@/pages/ShippingPage.vue'),
        meta: {
          title: 'Execute Shipping',
          requiresAuth: true,
          requiresPerm: 'can_access_shipping'
        }
      },
      {
        name: 'shipping-manifest',
        path: 'shipping/:jobId/manifest',
        component: () => import('@/components/Shipping/ShippingManifest.vue'),
        meta: {
          title: 'Shipping Manifest',
          requiresAuth: true,
          requiresPerm: 'can_access_shipping'
        }
      },
      {
        name: 'shelving-move',
        path: 'shelving/move/:type/:jobId?',
        component: () => import('@/components/Shelving/ShelvingMoveExecute.vue'),
        meta: {
          title: 'Shelving Move',
          requiresAuth: true,
          requiresPerm: 'can_move_trays_and_items_shelving_locations'
        }
      },
      {
        name: 'ShelveByListCreate',
        path: 'shelving/list/create',
        component: () => import('@/components/Shelving/ShelvingListCreate.vue'),
        meta: {
          title: 'Create Shelve by List',
          requiresAuth: true,
          requiresPerm: 'can_create_and_execute_shelving_job'
        }
      },
      {
        name: 'ShelveByListExecute',
        path: 'shelving/list/:id',
        component: () => import('@/components/Shelving/ShelvingListExecute.vue'),
        meta: {
          title: 'Shelve by List',
          requiresAuth: true,
          requiresPerm: 'can_create_and_execute_shelving_job'
        }
      },
      {
        name: 'user-settings',
        path: 'user/settings',
        component: () => import('@/pages/UserSettings.vue'),
        meta: {
          title: 'User Settings',
          requiresAuth: true
        }
      },
      ...(import.meta.env.VITE_ENV !== 'production' ? [
        {
          name: 'test',
          path: '/test',
          component: () => import('@/pages/TestPage.vue'),
          meta: {
            title: 'Test Page'
          }
        }
      ] : []),
      {
        name: 'verification',
        path: 'verification/:jobId?',
        component: () => import('@/pages/VerificationPage.vue'),
        meta: {
          title: 'Verification',
          requiresAuth: true,
          requiresPerm: 'can_access_verification'
        }
      },
      {
        name: 'verification-container',
        path: 'verification/:jobId?/scan-items/:containerId?',
        component: () => import('@/pages/VerificationPage.vue'),
        beforeEnter ({ params }) {
          if (!params.containerId) {
            return {
              name: 'verification',
              params: {
                jobId: params.jobId
              }
            }
          }
        },
        meta: {
          title: 'Verification Container',
          requiresAuth: true,
          requiresPerm: 'can_access_verification'
        }
      },
      {
        name: 'withdrawal',
        path: 'withdrawal/:jobId?',
        component: () => import('@/pages/WithdrawalPage.vue'),
        meta: {
          title: 'Withdrawal',
          requiresAuth: true,
          requiresPerm: 'can_access_withdraw'
        }
      }
    ]
  },
  // Always leave this as last one,
  // 404 page for unknown links
  {
    path: '/:catchAll(.*)*',
    component: () => import('@/pages/ErrorNotFound.vue'),
    meta: {
      title: 'Page Not Found'
    }
  }
]

export default routes
