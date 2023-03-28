SET ROLE tq_admin;

-- words in a wordgram
CREATE TABLE IF NOT EXISTS public.node (
  id 		BIGINT PRIMARY KEY NOT NULL,
  words 	TEXT NOT NULL,
  pos		TEXT,	-- comma delimited string array
  topicid	TEXT,	-- comma delimited string array
  dbpedia	TEXT,
  wikidata 	TEXT,
  tense		TEXT,	--eg past
  negation	BOOLEAN DEFAULT false,
  epi		TEXT,	-- null , speculative, ...
  active	BIGINT REFERENCES  public.node (id),
  cannon	BIGINT REFERENCES  public.node (id)
);
CREATE INDEX IF NOT EXISTS node_idx ON public.node (id);

CREATE TABLE IF NOT EXISTS public.inlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.node (id),
  	isentenceId	TEXT NOT NULL,
  	itargetId	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS inlinks_idx ON public.inlinks (id, isentenceId);

CREATE TABLE IF NOT EXISTS public.outlinks (
  	id BIGINT PRIMARY KEY NOT NULL REFERENCES  public.node (id),
  	osentenceId	TEXT NOT NULL,
  	otargetId	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS outlinks_idx ON public.outlinks (id, osentenceId);

-- for expansion as needed = keys in this table need to be distinctive, e.g. starting with a "_" 
CREATE TABLE IF NOT EXISTS public.properties (
  id 	BIGINT PRIMARY KEY NOT NULL REFERENCES  public.node (id),
  _key 	TEXT NOT NULL,
  _val	TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS properties_idx ON public.properties (id, _key);
