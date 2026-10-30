BEGIN;
CREATE EXTENSION IF NOT EXISTS pgtap WITH SCHEMA extensions;
SET search_path = public, extensions;
SELECT plan(15);
SELECT ok(NOT has_function_privilege('anon', 'public.get_or_create_customer_qr_token()', 'EXECUTE'), 'Guest cannot recover QR');
SELECT ok(has_function_privilege('authenticated', 'public.get_or_create_customer_qr_token()', 'EXECUTE'), 'Customer can call recovery');
SELECT ok(has_function_privilege('authenticated', 'private_customer.get_or_create_qr()', 'EXECUTE'), 'Invoker wrapper can call private helper');
SELECT ok(NOT has_table_privilege('authenticated', 'private_customer.qr_secrets', 'SELECT'), 'Mapping not readable');
SELECT ok(NOT has_table_privilege('authenticated', 'vault.decrypted_secrets', 'SELECT'), 'Vault not readable');
SELECT ok(NOT has_function_privilege('authenticated', 'public.rotate_customer_qr_token()', 'EXECUTE'), 'Legacy rotation revoked');

INSERT INTO auth.users (id, email, email_confirmed_at)
VALUES ('b1000000-0000-0000-0000-000000000001', 'qr-one@example.invalid', now()),
       ('b1000000-0000-0000-0000-000000000002', 'qr-two@example.invalid', now()),
       ('b1000000-0000-0000-0000-000000000003', 'qr-unverified@example.invalid', null);
CREATE TEMP TABLE cards (owner text, value jsonb);
GRANT ALL ON TABLE cards TO authenticated;
SET LOCAL ROLE authenticated;
SELECT set_config('request.jwt.claim.sub', 'b1000000-0000-0000-0000-000000000001', true);
SELECT public.ensure_customer_profile('QR One', NULL);
INSERT INTO cards VALUES ('one', public.get_or_create_customer_qr_token());
SELECT matches((SELECT value->>'qr_token' FROM cards WHERE owner='one'), '^[0-9a-f]{64}$', 'Opaque 256-bit token');
SELECT is(public.get_or_create_customer_qr_token(), (SELECT value FROM cards WHERE owner='one'), 'Retry returns same token');
SELECT public.update_customer_profile('QR One Updated', '5512345678');
SELECT is(public.get_or_create_customer_qr_token(), (SELECT value FROM cards WHERE owner='one'), 'Profile update preserves QR');
SELECT set_config('request.jwt.claim.sub', 'b1000000-0000-0000-0000-000000000002', true);
SELECT public.ensure_customer_profile('QR Two', NULL);
INSERT INTO cards VALUES ('two', public.get_or_create_customer_qr_token());
SELECT isnt((SELECT value->>'qr_token' FROM cards WHERE owner='one'), (SELECT value->>'qr_token' FROM cards WHERE owner='two'), 'Different accounts have different tokens');
SELECT set_config('request.jwt.claim.sub', 'b1000000-0000-0000-0000-000000000001', true);
SELECT is(public.get_or_create_customer_qr_token(), (SELECT value FROM cards WHERE owner='one'), 'Returning account recovers original');
SELECT set_config('request.jwt.claim.sub', 'b1000000-0000-0000-0000-000000000003', true);
SELECT throws_ok('SELECT public.get_or_create_customer_qr_token()', '42501', 'verified_customer_required', 'Unverified identity denied');
RESET ROLE;
SELECT is((SELECT count(*)::integer FROM private_customer.qr_secrets WHERE cliente_id IN (SELECT (value->>'cliente_id')::uuid FROM cards)), 2, 'Exactly one secret per customer');
SELECT ok((SELECT bool_and(c.qr_token_hash = encode(extensions.digest(t.value->>'qr_token', 'sha256'), 'hex')) FROM cards t JOIN public.clientes c ON c.id=(t.value->>'cliente_id')::uuid), 'Manager hash remains compatible');
-- Simulate a legacy card whose plaintext was never retained.
DELETE FROM private_customer.qr_secrets WHERE cliente_id=(SELECT (value->>'cliente_id')::uuid FROM cards WHERE owner='two');
SET LOCAL ROLE authenticated;
SELECT set_config('request.jwt.claim.sub', 'b1000000-0000-0000-0000-000000000002', true);
SELECT throws_ok('SELECT public.get_or_create_customer_qr_token()', 'P0001', 'legacy_qr_requires_recovery', 'Missing secret never silently rotates');
RESET ROLE;
SELECT * FROM finish();
ROLLBACK;
