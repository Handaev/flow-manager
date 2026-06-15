INSERT INTO conversion_file (id, from_extension, to_extension, path, status, created_at, name)
VALUES (101, 'txt', 'pdf', 'sources', 'SUCCESS', CURRENT_TIMESTAMP, 'file.txt');

INSERT INTO conversion_file (id, from_extension, to_extension, path, status, created_at, name)
VALUES (102, 'pdf', null, 'pdf', 'SUCCESS', CURRENT_TIMESTAMP, 'file.pdf');

INSERT INTO conversion_file (id, from_extension, to_extension, path, status, created_at, name)
VALUES (103, 'txt', 'pdf', 'sources', 'IN_PROCESSING', CURRENT_TIMESTAMP, 'file.txt');