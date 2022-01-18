import API from '@/services'
import {
  GraphMetricsPayload,
  GraphMetricsResponse,
  PreFabGraph,
  Resource,
  ResourceDefinitionsApiResponse,
  VuexContext
} from '@/types'
import { uniq } from 'lodash'
import { ActionContext } from 'vuex'
import { State } from './state'

const getGraphDefinitionsByResourceIds = async (context: ActionContext<State, any>, ids: string[]) => {
  let idsWithDefinitions: { id: string; definitions: string[]; label: string }[] = []
  const resourceAndPromises: { [x: string]: Promise<ResourceDefinitionsApiResponse>[] } = {}
  // list from resourceModule
  const resources: Resource[] = JSON.parse(
    JSON.stringify(context.rootState.resourceModule.nodeResource.children.resource)
  )

  const getLabelFromId = (id: string) => {
    for (const resource of resources) {
      if (resource.id === id) {
        return resource.label
      }
    }
    return ''
  }

  for (const id of ids) {
    if (resourceAndPromises[id]) {
      resourceAndPromises[id] = [...resourceAndPromises[id], API.getGraphDefinitionsByResourceId(id)]
    } else {
      resourceAndPromises[id] = [API.getGraphDefinitionsByResourceId(id)]
    }
  }

  for (const id in resourceAndPromises) {
    let definitions: string[] = []
    const promises = resourceAndPromises[id]
    const results = await Promise.all(promises)
    for (const result of results) {
      definitions = [...definitions, ...result.name.sort((a, b) => a.localeCompare(b))]
    }
    const uniqueSortedDefinitions = uniq(definitions)
    idsWithDefinitions = [
      ...idsWithDefinitions,
      { id, definitions: uniqueSortedDefinitions, label: getLabelFromId(id) }
    ]
  }

  const definitionsList = uniq(idsWithDefinitions.map((item) => item.definitions).flat())

  context.commit('SAVE_DEFINITIONS_LIST', definitionsList)
  context.commit('SAVE_DEFINITIONS', idsWithDefinitions)
}

const getDefinitionData = async (context: VuexContext, definition: string): Promise<PreFabGraph | null> => {
  const definitionData = await API.getDefinitionData(definition)

  if (definitionData) {
    context.commit('SAVE_DEFINITION_DATA', definitionData)
  }

  return definitionData
}

const getGraphMetrics = async (
  context: VuexContext,
  payload: GraphMetricsPayload
): Promise<GraphMetricsResponse | null> => {
  const metrics = await API.getGraphMetrics(payload)

  if (metrics) {
    context.commit('SAVE_GRAPH_METRICS', metrics)
  }

  return metrics
}

export default {
  getGraphDefinitionsByResourceIds,
  getDefinitionData,
  getGraphMetrics
}
