create table if not exists hist (
  path varchar(500) not null,
  name varchar(100) not null,
  project varchar(100) not null,
  mdate timestamp not null
);

create table if not exists tags (
  id integer primary key auto_increment,
  path varchar(500) not null,
  tags varchar(500) not null
);

create table if not exists tagstat (
  tag varchar(100) primary key,
  occurences integer not null
)
