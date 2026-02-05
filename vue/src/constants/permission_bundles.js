export const PERMISSION_BUNDLES = [
  {
    name: 'Accessioning (Viewer)',
    description: 'Read-only access to Accession Dashboard and Job details.',
    permissions: ['can_access_accession']
  },
  {
    name: 'Accessioning (Core)',
    description: 'Create and Process jobs (includes View access).',
    permissions: [
      'can_access_accession',
      'create_accession_jobs',
      'can_access_item_detail',
      'can_access_tray_detail'
    ]
  },
  {
    name: 'Accessioning (Delete)',
    description: 'Delete Accession Jobs.',
    permissions: ['delete_accession_jobs']
  },
  {
    name: 'Verification (Viewer)',
    description: 'Read-only access to Verification Dashboard.',
    permissions: ['can_access_verification']
  },
  {
    name: 'Verification (Core)',
    description: 'Verify items, view details.',
    permissions: [
      'can_access_verification',
      'create_verification_jobs',
      'process_verification_jobs',
      'can_access_item_detail'
    ]
  },
  {
    name: 'Verification (Delete)',
    description: 'Cancel/Delete Verification Jobs.',
    permissions: ['delete_verification_jobs']
  },
  {
    name: 'Shelving (Viewer)',
    description: 'Read-only access to Shelving Dashboard.',
    permissions: ['can_access_shelving']
  },
  {
    name: 'Shelving (Core)',
    description: 'Create/Process shelving jobs (includes View access).',
    permissions: [
      'can_access_shelving',
      'can_create_and_execute_shelving_job',
      'can_create_and_execute_direct_shelving_job',
      'can_move_trays_and_items',
      'can_move_trays_and_items_shelving_locations',
      'process_shelving_jobs',
      'can_access_shelf_detail'
    ]
  },
  {
    name: 'Shelving (Assign)',
    description: 'Reassign users on shelving jobs.',
    permissions: [
      'can_assign_and_reassign_shelving_job',
      'manage_shelving_assignments'
    ]
  },
  {
    name: 'Shelving (Delete)',
    description: 'Delete Shelving Jobs.',
    permissions: ['delete_shelving_jobs']
  },
  {
    name: 'Requests (Viewer)',
    description: 'Read-only access to Request Dashboard.',
    permissions: ['can_access_request']
  },
  {
    name: 'Requests (Core)',
    description: 'Create and submit requests.',
    permissions: [
      'can_access_request',
      'can_create_and_submit_manual_requests',
      'can_create_and_submit_batch_requests',
      'can_perform_batch_uploads'
    ]
  },
  {
    name: 'Requests (Delete)',
    description: 'Delete requests.',
    permissions: ['delete_requests']
  },
  {
    name: 'Picklist (Viewer)',
    description: 'Read-only access to Picklist Dashboard.',
    permissions: ['can_access_picklist']
  },
  {
    name: 'Picklist (Core)',
    description: 'Create/Process pick lists.',
    permissions: [
      'can_access_picklist',
      'can_create_picklist_job',
      'can_add_to_picklist_job',
      'process_pick_lists',
      'can_edit_picklist_job'
    ]
  },
  {
    name: 'Picklist (Assign)',
    description: 'Reassign Picklist Jobs.',
    permissions: [
      'can_assign_and_reassign_picklist_job',
      'manage_picklist_assignments'
    ]
  },
  {
    name: 'Picklist (Delete)',
    description: 'Delete Picklist Jobs.',
    permissions: ['delete_pick_lists']
  },
  {
    name: 'Refile (Viewer)',
    description: 'Read-only access to Refile Dashboard.',
    permissions: ['can_access_refile']
  },
  {
    name: 'Refile (Core)',
    description: 'Create and execute refile jobs, view queue.',
    permissions: [
      'can_access_refile',
      'can_create_refile_job',
      'can_add_refile_item_to_queue',
      'can_add_to_refile_job',
      'can_edit_refile_job',
      'process_refile_jobs',
      'can_execute_and_complete_refile_job'
    ]
  },
  {
    name: 'Refile (Assign)',
    description: 'Reassign Refile Jobs.',
    permissions: [
      'can_assign_and_reassign_refile_job',
      'manage_refile_assignments'
    ]
  },
  {
    name: 'Refile (Delete)',
    description: 'Delete Refile Jobs.',
    permissions: ['delete_refile_jobs']
  },
  {
    name: 'Withdraw (Viewer)',
    description: 'Read-only access to Withdraw Dashboard.',
    permissions: ['can_access_withdraw']
  },
  {
    name: 'Withdraw (Core)',
    description: 'Create and process withdraw jobs.',
    permissions: ['can_access_withdraw']
  },
  {
    name: 'Reporting',
    description: 'View reports, searches, and audits.',
    permissions: [
      'can_access_reports',
      'can_access_search',
      'can_view_all_shelving_jobs',
      'can_view_all_picklist_jobs',
      'can_view_all_refile_jobs'
    ]
  },
  {
    name: 'User Administration',
    description: 'Manage Users, Groups, and Permissions.',
    permissions: [
      'can_manage_groups_and_permissions',
      'can_access_admin'
    ]
  },
  {
    name: 'Location Management',
    description: 'Create/Edit Buildings, Aisles, Shelves.',
    permissions: ['can_manage_locations']
  },
  {
    name: 'Item Management',
    description: 'Edit Tray and Non-Tray Item details.',
    permissions: [
      'can_edit_tray',
      'can_edit_non_tray_item'
    ]
  },
  {
    name: 'System Configuration',
    description: 'Global Settings, Owner Options, List Configs.',
    permissions: [
      'can_manage_system_configurations',
      'can_manage_list_configurations',
      'can_access_admin'
    ]
  }
]
