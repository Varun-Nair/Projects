use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

//use std::iter::Map;
//use std::collections::HashMap;
use cosmwasm_std::Addr;
use cw_storage_plus::{Item, Map};


#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct State {
    pub count: i32,
    pub owner: Addr,
}

pub const STATE: Item<State> = Item::new("state");
pub const SCORES: Map<&Addr, u8> = Map::new("scores");
