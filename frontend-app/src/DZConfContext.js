import React from 'react'

const DZConfContext = React.createContext()

export const DZConfProvider = DZConfContext.Provider
export const DZConfConsumer = DZConfContext.Consumer

export default DZConfContext