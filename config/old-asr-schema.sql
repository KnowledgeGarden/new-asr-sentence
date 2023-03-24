SET ROLE tq_admin;



-- words in a wordgram
CREATE TABLE IF NOT EXISTS public.words (
  id BIGINT PRIMARY KEY NOT NULL,
  words TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS words_idx ON public.words (id);



-- index title, text id

CREATE TABLE IF NOT EXISTS public.pos (
  id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  pos VARCHAR(3) NOT NULL
);


CREATE UNIQUE INDEX IF NOT EXISTS pos_idx ON public.pos (id, pos);

CREATE TABLE IF NOT EXISTS public.topicid (
	id BIGINT NOT NULl REFERENCES  public.words (id),
	locator TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS topicid_idx ON public.topicid (id, locator);


CREATE TABLE IF NOT EXISTS public.dbpedia (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	data	TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS dbpedia_idx ON public.dbpedia (id);

CREATE TABLE IF NOT EXISTS public.wikidata (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	data	TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS wikidata_idx ON public.dbpedia (id);

CREATE TABLE IF NOT EXISTS public.inlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	link	BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS inlinks_idx ON public.dbpedia (id);

CREATE TABLE IF NOT EXISTS public.outlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	data	BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS outlinks_idx ON public.dbpedia (id);

CREATE TABLE IF NOT EXISTS public.active (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	active	BIGINT NOT NULL REFERENCES  public.words (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS active_idx ON public.active (id);

CREATE TABLE IF NOT EXISTS public.cannon (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.words (id),
  	cannon	BIGINT NOT NULL REFERENCES  public.words (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS cannon_idx ON public.cannon (id);

