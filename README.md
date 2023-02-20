# NEI GraphQL

A GraphQL API for the NotEnoughItem (NEI) crafting database!
This is designed to work with database exported from [D-Cysteine/nesql-exporter](https://github.com/D-Cysteine/nesql-exporter).

Written in Kotlin with [Ktor](https://ktor.io/), [ktorm](https://www.ktorm.org/), and [kGraphQL](https://kgraphql.io/).

The schema is available at [here](https://github.com/harrynull/webnei/blob/main/schema.graphql),
or you could just use the introspection schema.

A demo is available at [here](https://harrynull.tech/nei-graphql/graphql).
There is no service availability guarantee, and please try not to abuse it
(e.g. keep the limit param a sane number).
See the [release page](https://github.com/harrynull/NEIGraphQL/releases) if you need a dump of the db.

A simple frontend for it is available at [here](https://webnei.harrynull.tech/) with
source code at [harrynull/webnei](https://github.com/harrynull/webnei).

Contributions are welcome!

## Features
- [x] Items
- [x] Recipes
- [ ] Fluids
- [ ] Quests
- [ ] Thaumcraft

## License
AGPLv3. See LICENSE.
