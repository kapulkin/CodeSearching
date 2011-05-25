CREATE TABLE convCodes (
	id BIGINT NOT NULL AUTO_INCREMENT,
	delay INT NOT NULL,
	inf_length INT NOT NULL,
	code_length INT NOT NULL,
	generator TEXT,
	parity_check TEXT,
	free_dist INT,
	PRIMARY KEY (id),
	CONSTRAINT matrix_completness CHECK (generator IS NOT NULL OR parity_check IS NOT NULL),
	CONSTRAINT values_positivity CHECK (delay > 0 AND inf_length > 0 AND code_length > 0)
);

CREATE TABLE articleConvCodes (
	id BIGINT NOT NULL AUTO_INCREMENT,
	delay INT NOT NULL,
	inf_length INT NOT NULL,
	code_length INT NOT NULL,
	generator TEXT,
	parity_check TEXT,
	free_dist INT,
	PRIMARY KEY (id),
	CONSTRAINT matrix_completness CHECK (generator IS NOT NULL OR parity_check IS NOT NULL),
	CONSTRAINT values_positivity CHECK (delay > 0 AND inf_length > 0 AND code_length > 0)
);

CREATE TABLE blockCodes (
	id BIGINT NOT NULL AUTO_INCREMENT,
	inf_length INT NOT NULL,
	code_length INT NOT NULL,
	generator TEXT,
	parity_check TEXT,
	min_dist INT, PRIMARY KEY (id),
	CONSTRAINT matrix_completness CHECK (generator IS NOT NULL OR parity_check IS NOT NULL),
	CONSTRAINT values_positivity CHECK (inf_length > 0 AND code_length > 0)
);