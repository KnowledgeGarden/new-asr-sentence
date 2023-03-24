SET ROLE tq_admin;

-- words in a wordgram
CREATE TABLE IF NOT EXISTS public.words (
  id BIGINT PRIMARY KEY NOT NULL,
  words TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS words_idx ON public.words (id);

CREATE TABLE IF NOT EXISTS public.pos (
  id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  pos VARCHAR(3) NOT NULL
);
CREATE INDEX IF NOT EXISTS pos_idx ON public.pos (id, pos);

CREATE TABLE IF NOT EXISTS public.topicid (
	id BIGINT NOT NULl REFERENCES  public.words (id),
	locator TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS topicid_idx ON public.topicid (id, locator);

CREATE TABLE IF NOT EXISTS public.dbpedia (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	dbp	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS dbpedia_idx ON public.dbpedia (id);

CREATE TABLE IF NOT EXISTS public.wikidata (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	wkd	TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS wikidata_idx ON public.wikidata (id);

CREATE TABLE IF NOT EXISTS public.inlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	isentenceId	TEXT NOT NULL,
  	itargetId	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS inlinks_idx ON public.inlinks (id);

CREATE TABLE IF NOT EXISTS public.outlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	osentenceId	TEXT NOT NULL,
  	otargetId	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS outlinks_idx ON public.outlinks (id);

-- passive verbs will link to an active verb
CREATE TABLE IF NOT EXISTS public.active (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	active	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS active_idx ON public.active (id);

-- some terms are replaced by a canonical term
CREATE TABLE IF NOT EXISTS public.cannon (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	cannon	TEXT NOT NULL 
);
CREATE INDEX IF NOT EXISTS cannon_idx ON public.cannon (id);

CREATE TABLE IF NOT EXISTS public.synonym (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	synonyn	TEXT NOT NULL 
);
CREATE INDEX IF NOT EXISTS synonym_idx ON public.synonym (id);

CREATE TABLE IF NOT EXISTS public.antonym (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	antonym	TEXT NOT NULL 
);
CREATE INDEX IF NOT EXISTS antonym_idx ON public.antonym (id);

-- for expansion as needed = keys in this table need to be distinctive, e.g. starting with a "_" 
CREATE TABLE IF NOT EXISTS public.properties (
  id 	BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  _key 	TEXT NOT NULL,
  _val	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS properties_idx ON public.properties (id);
