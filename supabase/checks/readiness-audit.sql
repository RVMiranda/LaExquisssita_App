SELECT jsonb_build_object(
  'customer_rpcs', (SELECT coalesce(jsonb_agg(p.proname), '[]'::jsonb) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace WHERE n.nspname='public' AND p.proname IN ('ensure_customer_profile','update_customer_profile','rotate_customer_qr_token','get_or_create_customer_qr_token')),
  'extensions', (SELECT jsonb_agg(jsonb_build_object('name', name, 'installed_version', installed_version)) FROM pg_available_extensions WHERE name IN ('supabase_vault','pgcrypto')),
  'public_promotion_read', has_table_privilege('anon', 'public.promociones', 'SELECT')
) AS readiness;
