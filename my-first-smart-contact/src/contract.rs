#[cfg(not(feature = "library"))]
use cosmwasm_std::entry_point;
use cosmwasm_std::{to_binary, Binary, Deps, DepsMut, Env, MessageInfo, Response, StdResult};
use cw2::set_contract_version;
//use std::collections::HashMap;
//use cosmwasm_std::Addr;
//use std::iter::Map;

use crate::error::ContractError;
use crate::msg::{OwnerResponse, CountResponse, ExecuteMsg, InstantiateMsg, QueryMsg};
use crate::state::{State, STATE, SCORES};

// version info for migration info
const CONTRACT_NAME: &str = "crates.io:my-first-contract";
const CONTRACT_VERSION: &str = env!("CARGO_PKG_VERSION");

#[cfg_attr(not(feature = "library"), entry_point)]
pub fn instantiate(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: InstantiateMsg,
) -> Result<Response, ContractError> {
    let state = State {
        count: msg.count,
        owner: info.sender.clone(),

    };
    set_contract_version(deps.storage, CONTRACT_NAME, CONTRACT_VERSION)?;
    STATE.save(deps.storage, &state)?;

    Ok(Response::new()
        .add_attribute("method", "instantiate")
        .add_attribute("owner", info.sender)
        .add_attribute("count", msg.count.to_string()))
}

#[cfg_attr(not(feature = "library"), entry_point)]
pub fn execute(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: ExecuteMsg,
) -> Result<Response, ContractError> {
    match msg {
        ExecuteMsg::Increment {} => try_increment(deps),
        ExecuteMsg::Reset { count } => try_reset(deps, info, count),
        ExecuteMsg::SetScore {addr, score} => try_set_score(deps, info, addr, score),
    }
}

pub fn try_set_score(deps: DepsMut, info: MessageInfo, addr: String, score: u8) -> Result<Response, ContractError> {
    //validate address
    let entered_address = deps.api.addr_validate(&addr)?;
    let state = STATE.load(deps.storage)?;
    let change_or_set_score = |current_score: Option<u8>| -> StdResult<u8> {
        match current_score {
            Some(_num) => Ok(score),
            None => Ok(score),
        }
    };
    //check whether sender is owner
    if info.sender != state.owner {
        return Err(ContractError::Unauthorized {});
    } else {
        SCORES.update(deps.storage, &entered_address, change_or_set_score)?;
    }
    
    Ok(Response::new().add_attribute("method", "score_set"))
}

pub fn try_increment(deps: DepsMut) -> Result<Response, ContractError> {
    STATE.update(deps.storage, |mut state| -> Result<_, ContractError> {
        state.count += 1;
        Ok(state)
    })?;

    Ok(Response::new().add_attribute("method", "try_increment"))
}
pub fn try_reset(deps: DepsMut, info: MessageInfo, count: i32) -> Result<Response, ContractError> {
    STATE.update(deps.storage, |mut state| -> Result<_, ContractError> {
        if info.sender != state.owner {
            return Err(ContractError::Unauthorized {});
        }
        state.count = count;
        Ok(state)
    })?;
    Ok(Response::new().add_attribute("method", "reset"))
}

#[cfg_attr(not(feature = "library"), entry_point)]
pub fn query(deps: Deps, _env: Env, msg: QueryMsg) -> StdResult<Binary> {
    match msg {
        QueryMsg::GetCount {} => to_binary(&query_count(deps)?),
        QueryMsg::GetOwner {} => to_binary(&query_owner(deps)?),
        QueryMsg::GetScore {addr} => {
            let valid_address = deps.api.addr_validate(&addr)?;
            let current_score = SCORES.load(deps.storage, &valid_address)?;
            to_binary(&current_score)
        },
    }
}

fn query_count(deps: Deps) -> StdResult<CountResponse> {
    let state = STATE.load(deps.storage)?;
    Ok(CountResponse { count: state.count })
}

fn query_owner(deps: Deps) -> StdResult<OwnerResponse> {
    let state = STATE.load(deps.storage)?;
    Ok(OwnerResponse { owner: state.owner})
}

#[cfg(test)]
mod tests {
    use super::*;
    use cosmwasm_std::testing::{mock_dependencies, mock_env, mock_info};
    use cosmwasm_std::{coins, from_binary};

    
    // Instantiates the contract and set the owner 
    // Tests the query to get owner   
    #[test]
    fn instantiate_and_get_owner_test() {
        let mut deps = mock_dependencies(&coins(2, "mirror"));

        let msg = InstantiateMsg { count: 17 };
        let info = mock_info("owner23", &coins(2, "mirror"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOwner {}).unwrap();
        let value: OwnerResponse = from_binary(&res).unwrap();

        assert_eq!("owner23", value.owner);
    
    }

    #[test]
    // Tests that owner can set owner's score
    // Tests the GetScore Query
    fn set_score_and_get_score_1() {
        let mut deps = mock_dependencies(&coins(2, "token"));

        let msg = InstantiateMsg { count: 17 };
        let info = mock_info("creator", &coins(2, "token"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // Set Score for Owner
        let auth_info = mock_info("creator", &coins(2, "token"));
        let msg = ExecuteMsg::SetScore {addr: "creator".to_string(), score: 15};
        let _res = execute(deps.as_mut(), mock_env(), auth_info, msg).unwrap();

        //TODO: Get Score Query for owner
        let res = query(deps.as_ref(), mock_env(), QueryMsg::GetScore {addr: "creator".to_string()}).unwrap();
        let value: u8 = from_binary(&res).unwrap();
        assert_eq!(15, value);
    }

    #[test]
    // Tests that owner can set anyone's score
    // Tests the GetScore Query
    fn set_score_and_get_score_2() {
        let mut deps = mock_dependencies(&coins(2, "token"));

        let msg = InstantiateMsg { count: 17 };
        let info = mock_info("creator", &coins(2, "token"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // only the original creator can set score the counter
        let auth_info = mock_info("creator", &coins(2, "token"));
        let msg = ExecuteMsg::SetScore {addr: "anyone".to_string(), score: 10};
        let _res = execute(deps.as_mut(), mock_env(), auth_info, msg).unwrap();

        //TODO: Get Score Query for anyone
        let res = query(deps.as_ref(), mock_env(), QueryMsg::GetScore {addr: "anyone".to_string()}).unwrap();
        let value: u8 = from_binary(&res).unwrap();
        assert_eq!(10, value);
        
    }

    #[test]
    // Tests that anyone cannot set any score
    // Tests the GetScore Query
    fn set_score_and_get_score_3() {
        let mut deps = mock_dependencies(&coins(2, "token"));

        let msg = InstantiateMsg { count: 17 };
        let info = mock_info("creator", &coins(2, "token"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();
        
        let unauth_info = mock_info("anyone", &coins(2, "token"));
        let msg = ExecuteMsg::SetScore {addr: "creator".to_string(), score: 10}; 
        let res = execute(deps.as_mut(), mock_env(), unauth_info, msg);
        match res {
            Err(ContractError::Unauthorized {}) => {}
            _ => panic!("Must return unauthorized error"),
        }

    }


    // --- Tests from Template ---

    // #[test]
    // fn proper_initialization() {
    //     let mut deps = mock_dependencies(&[]);

    //     let msg = InstantiateMsg { count: 17 };
    //     let info = mock_info("creator", &coins(1000, "earth"));

    //     // we can just call .unwrap() to assert this was a success
    //     let res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();
    //     assert_eq!(0, res.messages.len());

    //     // it worked, let's query the state
    //     let res = query(deps.as_ref(), mock_env(), QueryMsg::GetCount {}).unwrap();
    //     let value: CountResponse = from_binary(&res).unwrap();
    //     assert_eq!(17, value.count);
    // }

    // #[test]
    // fn increment() {
    //     let mut deps = mock_dependencies(&coins(2, "token"));

    //     let msg = InstantiateMsg {count: 17 };
    //     let info = mock_info("creator", &coins(2, "token"));
    //     let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

    //     // beneficiary can release it
    //     let info = mock_info("anyone", &coins(2, "token"));
    //     let msg = ExecuteMsg::Increment {};
    //     let _res = execute(deps.as_mut(), mock_env(), info, msg).unwrap();

    //     // should increase counter by 1
    //     let res = query(deps.as_ref(), mock_env(), QueryMsg::GetCount {}).unwrap();
    //     let value: CountResponse = from_binary(&res).unwrap();
    //     assert_eq!(18, value.count);
    // }

    // #[test]
    // fn reset() {
    //     let mut deps = mock_dependencies(&coins(2, "token"));

    //     let msg = InstantiateMsg { count: 17 };
    //     let info = mock_info("creator", &coins(2, "token"));
    //     let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

    //     // beneficiary can release it
    //     let unauth_info = mock_info("anyone", &coins(2, "token"));
    //     let msg = ExecuteMsg::Reset { count: 5 };
    //     let res = execute(deps.as_mut(), mock_env(), unauth_info, msg);
    //     match res {
    //         Err(ContractError::Unauthorized {}) => {}
    //         _ => panic!("Must return unauthorized error"),
    //     }

    //     // only the original creator can reset the counter
    //     let auth_info = mock_info("creator", &coins(2, "token"));
    //     let msg = ExecuteMsg::Reset { count: 5 };
    //     let _res = execute(deps.as_mut(), mock_env(), auth_info, msg).unwrap();

    //     // should now be 5
    //     let res = query(deps.as_ref(), mock_env(), QueryMsg::GetCount {}).unwrap();
    //     let value: CountResponse = from_binary(&res).unwrap();
    //     assert_eq!(5, value.count);
    // }

    
}
